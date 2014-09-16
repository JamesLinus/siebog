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

package siebog.dnars.base

/**
 * Definition of a statement: subject, copula, predicate, frequency, and confidence.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
case class Statement(val subj: Term, val copula: String, val pred: Term, val truth: Truth) extends Serializable {
	lazy val id = s"$subj $copula $pred $truth"
	
	def equivalent(other: Statement): Boolean = 
		subj == other.subj && copula == other.copula && pred == other.pred && truth.closeTo(other.truth)
	
	override def toString = id
}

object Copula {
	val Inherit: String = "->"
	val Similar: String = "~"
}