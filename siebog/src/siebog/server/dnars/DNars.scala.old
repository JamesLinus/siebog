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

package dnars.actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import spray.can.Http
import spray.http.ContentType.apply
import spray.http.HttpEntity
import spray.http.HttpResponse
import spray.http.MediaTypes._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import spray.routing.HttpService

/**
 * Implementation of the RESTful web service.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait Service extends HttpService {
	/*lazy val reasoner = actorRefFactory.actorFor("akka://D-NARS/user/Reasoner")
	implicit val timeout = Timeout(5 seconds)
	implicit val ec = ExecutionContext.Implicits.global*/

	def route = {
		pathSingleSlash {
			get { complete(index) }
		} /*~
		path("question") {
			get {
				parameter('q) { q =>
					complete("ok") // DNars.question(q))
				}
			}
		} ~
		pathPrefix("table" / """[a-zA-z]+""".r) { tableName =>
			path("") {
				put {
					formField('statements.as[String]) { statements =>
						try {
							//SchemaCreator.create(tableName, statements)
							complete(Created, """{ "status": ok }""")
						} catch {
							case _: TableExistsException =>
								complete(Created, """{ "status": ok }""")
							case e: Exception =>
								val msg = e.getMessage; 
								complete(InternalServerError, s"""{ "msg": "$msg" }""")
						}
					}
				}
			}
		}*/
	}

	lazy val index = HttpResponse(entity = HttpEntity(`text/html`, <html><body>Hey!</body></html>.toString))
}

class ServiceActor extends Actor with Service {
	def actorRefFactory = context

	def receive = runRoute(route)
}

object DNars {
	implicit val system = ActorSystem("dnars")
	var host = "localhost"
	var port = 8080
	
	def main(args: Array[String]) {
		if (args.length == 2) {
			host = args(0)
			port = args(1).toInt
		}
		start(host, port)
	}
	
	def start(host: String, port: Int) {
		val service = system.actorOf(Props[ServiceActor], "Service")
		IO(Http) ! Http.Bind(service, host, port)
	}
	
	def stop() {
		system.shutdown
	}
}