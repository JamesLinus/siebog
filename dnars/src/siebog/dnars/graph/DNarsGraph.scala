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

package siebog.dnars.graph

import org.apache.commons.configuration.BaseConfiguration
import org.apache.commons.configuration.Configuration
import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.core.TitanGraph
import com.thinkaurelius.titan.core.Order
import com.thinkaurelius.titan.core.util.TitanCleanup
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala.ScalaGraph
import siebog.dnars.base.Statement
import siebog.dnars.events.EventManager
import siebog.dnars.graph.Wrappers.edge2DNarsEdge
import siebog.dnars.graph.Wrappers.vertex2DNarsVertex
import com.tinkerpop.blueprints.GraphQuery
import siebog.dnars.inference.forward.DeductionAnalogy
import siebog.dnars.inference.forward.AnalogyResemblance
import siebog.dnars.inference.forward.AbductionComparisonAnalogy
import siebog.dnars.inference.forward.InductionComparison
import siebog.dnars.inference.forward.AnalogyInv
import siebog.dnars.inference.ResolutionEngine
import siebog.dnars.inference.BackwardInference
import siebog.dnars.base.Copula
import siebog.dnars.base.AtomicTerm
import com.thinkaurelius.titan.core.schema.TitanManagement
import com.thinkaurelius.titan.core.schema.EdgeLabelMaker
import com.thinkaurelius.titan.core.EdgeLabel
import com.thinkaurelius.titan.core.PropertyKey
import com.thinkaurelius.titan.core.SchemaViolationException

/**
 * Wrapper around the ScalaGraph class. Inspired by
 * <a href="https://github.com/mpollmeier/gremlin-scala">gremlin/scala project</a>.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class DNarsGraph(override val graph: Graph, val domain: String) extends ScalaGraph(graph)
	with DNarsGraphAPI with VertexAPI with EdgeAPI with StatementAPI with EventManager
	with ResolutionEngine with BackwardInference {

	val engines = List(
		new DeductionAnalogy(this),
		new AnalogyResemblance(this),
		new AbductionComparisonAnalogy(this),
		new InductionComparison(this),
		new AnalogyInv(this))

	override def conclusions(input: Array[Statement]): Array[Statement] = {
		val inputList = input.toList // for compatibility with Java
		val result = engines.flatMap { engine => engine.apply(inputList) }
		result.toArray
	}

	override def conclusions(input: Statement): Array[Statement] =
		conclusions(Array(input))

	override def include(input: Array[Statement]): Unit = {
		val concl = conclusions(input)
		add(input.toList ::: concl.toList)
	}

	override def query(): GraphQuery = graph.query()

	def shutdown(): Unit = graph.shutdown()

	def clear(): Unit = graph match {
		case tg: TitanGraph =>
			TitanCleanup.clear(tg)
		case any: Any =>
			throw new IllegalArgumentException(any.getClass.getName + " cannot be cleared")
	}

	/**
	 * Debugging purposes only.
	 */
	def printEdges(): Unit = {
		println(s"---------------- Graph dump [domain=$domain] ----------------")
		forEachStatement(println(_))
		println("------------------- Done -------------------")
	}

	/**
	 * Debugging purposes only.
	 */
	def forEachStatement(f: (Statement) => Unit): Unit = {
		V
		val allSt = E.map { e =>
			val s: DNarsVertex = e.getVertex(Direction.OUT)
			val p: DNarsVertex = e.getVertex(Direction.IN)
			val c = e.getLabel
			val t = e.truth
			val st = Statement(s.term, c, p.term, t)
			// print only the packed version
			st.pack() match {
				case List() => st
				case List(h, _) => h
			}
		}.toSet
		allSt.foreach(f)
	}

	/**
	 * Debugging purposes only.
	 */
	def getEdgeCount(): Long = {
		var count = 0L
		V.as("x").inE.sideEffect { e => count += 1 }.back("x").outE.sideEffect { e => count += 1 }.iterate
		count
	}
}

object DNarsGraph {
	def apply(graph: ScalaGraph, keyspace: String) = wrap(graph, keyspace)
	implicit def wrap(graph: ScalaGraph, keyspace: String) = new DNarsGraph(graph, keyspace)
	implicit def unwrap(wrapper: DNarsGraph) = wrapper.graph
}

object DNarsGraphFactory {
	def create(domain: String, additionalConfig: java.util.Map[String, Any] = null): DNarsGraph = {
		val conf = getConfig(domain, additionalConfig)
		val graph = TitanFactory.open(conf)
		addVertexKey(graph)
		try {
			val keys = getOrderKeys(graph)
			addEdgeIndex(graph, Copula.Inherit, keys)
			addEdgeIndex(graph, Copula.Similar, keys)
		} catch {
			case _: SchemaViolationException => // keys already exist
			case e: Exception => throw e
		}
		DNarsGraph(ScalaGraph(graph), domain)
	}

	private def getConfig(domain: String, additionalConfig: java.util.Map[String, Any]): Configuration = {
		val conf = new BaseConfiguration
		conf.setProperty("storage.backend", "cassandrathrift")
		conf.setProperty("storage.hostname", "localhost");
		conf.setProperty("storage.cassandra.keyspace", domain)
		withAdditionalConfig(conf, additionalConfig)
	}

	private def withAdditionalConfig(conf: BaseConfiguration, additionalConfig: java.util.Map[String, Any]): BaseConfiguration = {
		if (additionalConfig != null) {
			val i = additionalConfig.entrySet().iterator
			while (i.hasNext()) {
				val c = i.next()
				conf.setProperty(c.getKey(), c.getValue())
			}
		}
		conf
	}

	private def addVertexKey(graph: TitanGraph): Unit = {
		try {
			val management = graph.getManagementSystem
			val key = management.makePropertyKey("term").dataType(classOf[String]).make()
			management.buildIndex("byTerm", classOf[Vertex]).addKey(key).unique().buildCompositeIndex()
		} catch {
			case _: SchemaViolationException => // already exists
			case e: Exception => throw e
		}
	}

	private def getOrderKeys(graph: TitanGraph): List[PropertyKey] =
		List(graph.makePropertyKey("subjExp").dataType(classOf[Integer]).make(),
			graph.makePropertyKey("predExp").dataType(classOf[Integer]).make())

	private def addEdgeIndex(graph: TitanGraph, copula: String, keys: List[PropertyKey]): Unit = {
		val management = graph.getManagementSystem
		val label = management.makeEdgeLabel(copula).make()
		keys.foreach { key: PropertyKey =>
			management.buildEdgeIndex(label, "by" + key.getName, Direction.BOTH, Order.DESC, key)
		}
	}
}