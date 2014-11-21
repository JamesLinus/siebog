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

import siebog.dnars.base.AtomicTerm.Placeholder
import siebog.dnars.base.Connector.ExtImage
import siebog.dnars.base.Connector.IntImage
import siebog.dnars.base.Connector.Product

object Copula {
	val Inherit: String = "->"
	val Similar: String = "~"
}

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

	import Copula._

	/**
	 * Performs structural transformation of a statement that includes compound
	 * terms with the product connector (e.g. "(cat x bird) -> eat" or
	 * "dissolve -> (water x salt)").
	 *
	 * @return List of 2 images, or an empty list if the input statement does not
	 * match any of the given forms.
	 */
	def unpack(): List[Statement] = this match {
		// (cat x bird) -> eat
		case Statement(CompoundTerm(Product, Seq(t1, t2)), Inherit, pred @ AtomicTerm(_), truth) =>
			// cat -> (/ eat * bird)
			val s1 = t1
			val p1 = CompoundTerm(ExtImage, List(pred, Placeholder, t2))
			// bird -> (/ eat cat *)
			val s2 = t2
			val p2 = CompoundTerm(ExtImage, List(pred, t1, Placeholder))
			//
			List(Statement(s1, Inherit, p1, truth), Statement(s2, Inherit, p2, truth))
		// eat -> (cat x bird)
		case Statement(subj @ AtomicTerm(_), Inherit, CompoundTerm(Product, Seq(t1, t2)), truth) =>
			// (\ eat * bird) -> cat
			val s1 = CompoundTerm(IntImage, List(subj, AtomicTerm.Placeholder, t2))
			val p1 = t1
			// (\ eat cat *) -> bird
			val s2 = CompoundTerm(IntImage, List(subj, t1, AtomicTerm.Placeholder))
			val p2 = t2
			//
			List(Statement(s1, Inherit, p1, truth), Statement(s2, Inherit, p2, truth))
		case _ =>
			List()
	}

	/**
	 * (Re)Combines intentional and extentional images back into statements,
	 * e.g. "cat -> (/ eat * bird)" becomes "(x cat bird) -> eat)". The other
	 * intentional/extentional image is created as well, in this example
	 * "bird -> (/ eat cat *)".
	 */
	def pack(): List[Statement] = this match {
		// cat -> (/ eat * bird)  => (x cat bird) -> eat, bird -> (/ eat cat *) 
		case Statement(subj1 @ AtomicTerm(_), Inherit, CompoundTerm(ExtImage, Seq(rel @ AtomicTerm(_), Placeholder, subj2 @ AtomicTerm(_))), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(ExtImage, List(rel, subj1, Placeholder))
			//
			List(Statement(cp1, Inherit, rel, truth), Statement(subj2, Inherit, cp2, truth))
		// bird -> (/ eat cat *) => (x cat bird) -> eat, cat -> (/ eat * bird)
		case Statement(subj2 @ AtomicTerm(_), Inherit, CompoundTerm(ExtImage, Seq(rel @ AtomicTerm(_), subj1 @ AtomicTerm(_), Placeholder)), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(ExtImage, List(rel, Placeholder, subj2))
			//
			List(Statement(cp1, Inherit, rel, truth), Statement(subj1, Inherit, cp2, truth))
		// (\ dissolve * solid) -> liquid => dissolve -> (x liquid solid), (\ dissolve liquid *) -> solid
		case Statement(CompoundTerm(IntImage, Seq(rel @ AtomicTerm(_), Placeholder, subj2 @ AtomicTerm(_))), Inherit, subj1 @ AtomicTerm(_), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(IntImage, List(rel, subj1, Placeholder))
			//
			List(Statement(rel, Inherit, cp1, truth), Statement(cp2, Inherit, subj2, truth))
		// (\ dissolve liquid *) -> solid => dissolve -> (x liquid solid), (\ dissolve * solid) -> liquid
		case Statement(CompoundTerm(IntImage, Seq(rel @ AtomicTerm(_), subj1 @ AtomicTerm(_), Placeholder)), Inherit, subj2 @ AtomicTerm(_), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(IntImage, List(rel, Placeholder, subj2))
			//
			List(Statement(rel, Inherit, cp1, truth), Statement(cp2, Inherit, subj1, truth))
		case _ =>
			List()
	}

	/**
	 * Returns all possible forms of a statement, e.g. the original form, and two extensional images.
	 */
	def allForms(): List[Statement] = {
		val images =
			pack() match {
				case a @ List(_, _) => a
				case _ => unpack() match {
					case b @ List(_, _) => b
					case _ => Nil
				}
			}
		this :: images
	}
}

