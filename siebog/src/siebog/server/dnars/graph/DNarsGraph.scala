/**
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information regarding 
 * copyright ownership. The ASF licenses this file to you under 
 * the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. 
 * 
 * See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package siebog.server.dnars.graph

import com.tinkerpop.gremlin.scala.ScalaGraph
import com.tinkerpop.blueprints.Graph
import siebog.server.dnars.base.Term
import com.tinkerpop.blueprints.Vertex
import siebog.server.dnars.base.Truth
import com.tinkerpop.blueprints.Edge
import siebog.server.dnars.base.Statement
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.blueprints.Direction
import siebog.server.dnars.base.CompoundTerm
import siebog.server.dnars.base.Connector._
import siebog.server.dnars.base.Copula._
import siebog.server.dnars.base.AtomicTerm
import org.apache.commons.configuration.BaseConfiguration
import com.thinkaurelius.titan.core.TitanFactory
import siebog.server.dnars.base.CompoundTerm
import siebog.server.dnars.base.CompoundTermSerializer
import siebog.server.dnars.base.AtomicTermSerializer
import siebog.server.dnars.base.TruthSerializer
import org.apache.commons.configuration.Configuration
import java.io.File
import java.nio.file.Files
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.thinkaurelius.titan.core.util.TitanCleanup
import com.thinkaurelius.titan.core.TitanGraph
import siebog.server.dnars.events.EventManager
import scala.collection.mutable.ListBuffer
import siebog.server.dnars.events.Event

/**
 * Wrapper around the ScalaGraph class. Inspired by 
 * <a href="https://github.com/mpollmeier/gremlin-scala">gremlin/scala project</a>.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class DNarsGraph(override val graph: Graph, val domain: String) extends ScalaGraph(graph) {
	val statements = new StatementManager(this)
	val eventManager = new EventManager()
	
	def getV(term: Term): Option[Vertex] = {
		val vertex = V.has("term", term.id).toList
		vertex match {
			case h :: Nil => Some(h)
			case _ => None
		}
	}

	/**
	 * Returns a vertex that corresponds to the given term. 
	 * If the vertex does not exist, it will added to the graph.
	 */
	def getOrAddV(term: Term): Vertex = {
		getV(term) match {
			case Some(v) => v
			case None =>
				val added = addV(null)
				DNarsVertex(added).term = term
				added
		}
	}
	
	def addE(subj: Vertex, copula: String, pred: Vertex, truth: Truth): Edge = {
		val edge = subj.addEdge(copula, pred)
		DNarsEdge(edge).truth = truth
		edge
	}
	
	def getE(st: Statement): Option[Edge] = {
		val s = getV(st.subj)
		val p = getV(st.pred)
		if (s == None || p == None) // no vertex, so no edge
			None
		else {
			// vertices exist, check for an edge
			val subj: ScalaVertex = s.get
			val list = subj.outE(st.copula).as("x").inV.retain(Seq(p.get)).back("x").toList
			list match {
				case List() => None // nope
				case h :: Nil => Some(h.asInstanceOf[Edge])
				case _ => throw new IllegalStateException(s"Multiple edges of the same copula for $st")
			}
		}
	}
	
	/**
	 * Debugging purposes only.
	 */
	def printEdges() {
		val list = E.map { e => {
			val s: DNarsVertex = e.getVertex(Direction.OUT)
			val p: DNarsVertex = e.getVertex(Direction.IN)
			val c = e.getLabel
			val t = DNarsEdge(e).truth
			val st = Statement(s.term, c, p.term, t)
			// print only the packed version
			statements.pack(st) match {
				case List() => st
				case List(h, _) => h
			}
		} }.toSet
		println(s"---------------- Graph dump [domain=$domain] ----------------")
		for (st <- list)
			println(st)
		println("------------------- Done -------------------")
	}
	
	def shutdown(clear: Boolean = false) = {
		graph.shutdown()
		if (clear)
			graph match {
			case tg: TitanGraph => 
				TitanCleanup.clear(tg)
			case any: Any => 
				throw new IllegalArgumentException(any.getClass.getName + " cannot be cleared")
		}
	}
}

object DNarsGraph {
	def apply(graph: ScalaGraph, keyspace: String) = wrap(graph, keyspace)
	implicit def wrap(graph: ScalaGraph, keyspace: String) = new DNarsGraph(graph, keyspace)
	implicit def unwrap(wrapper: DNarsGraph) = wrapper.graph
}

object DNarsGraphFactory {
	def create(domain: String, additionalConfig: Map[String, Any] = null): DNarsGraph = {
		val conf = getConfig(domain, additionalConfig)
		val graph = TitanFactory.open(conf)
		try {
			graph.makeKey("term").dataType(classOf[String]).indexed(classOf[Vertex]).unique().make()
		} catch {
			case _: IllegalArgumentException => 
			case e: Throwable => throw e 
		}
		DNarsGraph(ScalaGraph(graph), domain)
	}
	
	private def getConfig(domain: String, additionalConfig: Map[String, Any]): Configuration = {
		val conf = new BaseConfiguration
		conf.setProperty("storage.backend", "cassandra")
		conf.setProperty("storage.hostname", "localhost");
		// storage.machine-id-appendix
		conf.setProperty("storage.keyspace", domain)
		// custom serializers
		conf.setProperty("attributes.allow-all", "true")
		conf.setProperty("attributes.attribute20",  classOf[AtomicTerm].getName)
		conf.setProperty("attributes.serializer20", classOf[AtomicTermSerializer].getName)
		conf.setProperty("attributes.attribute21",  classOf[CompoundTerm].getName)
		conf.setProperty("attributes.serializer21", classOf[CompoundTermSerializer].getName)
		conf.setProperty("attributes.attribute22",  classOf[Truth].getName)
		conf.setProperty("attributes.serializer22", classOf[TruthSerializer].getName)
		// additional configuration?
		if (additionalConfig != null)
			additionalConfig.foreach { e => conf.setProperty(e._1 , e._2) }
		// done
		conf
	}
}