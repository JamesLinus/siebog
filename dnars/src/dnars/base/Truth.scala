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

package dnars.base

/**
 * Truth-value with accompanying functions.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */

object Truth {
	val K = 1
	val EPSILON = 0.01
}

case class Truth(val freq: Double, val conf: Double) {
	// used in comparisons, hash code calculations etc.   
	val freqInt = (freq * 100).toInt
	val confInt = (conf * 100).toInt
	
	def deduction(other: Truth): Truth = {
		val f = freq * other.freq
		Truth(f, f * conf * other.conf)
	}
	
	def induction(other: Truth): Truth = {
		val i = other.freq * conf * other.conf
		Truth(freq, i / (i + Truth.K))
	}
	
	def abduction(other: Truth): Truth = {
		val i = freq * conf * other.conf
		Truth(other.freq, i / (i + Truth.K))
	}
	
	def comparison(other: Truth): Truth = {
		val i = freq * other.freq
		val j = freq + other.freq - i
		val k = j * conf * other.conf
		Truth(i / j, k / (k + Truth.K))
	}
	
	def analogy(other: Truth, inv: Boolean): Truth = { 
		val f = freq * other.freq
		val c = conf * other.conf
		if (inv) 
			Truth(f, freq * c) 
		else 
			Truth(f, other.freq * c)
	}
	
	def resemblance(other: Truth): Truth = {
		val f = freq * other.freq
		Truth(f, (freq + other.freq - f) * conf * other.conf)
	}
	
	def revision(other: Truth): Truth = {
		val fb = (freq * conf * (1 - other.conf) + other.freq * other.conf * (1 - conf))
		val fi = conf * (1 - other.conf) + other.conf * (1 - conf)
		val cb = conf * (1 - other.conf) + other.conf * (1 - conf)
		val ci = cb + (1 - conf) * (1 - other.conf)
		Truth(fb / fi, cb / ci)
	}
	
	def choice(other: Truth): Truth = 
		if (conf > other.conf)
			Truth(freq, conf)
		else
			Truth(other.freq, other.conf)
	
	def expectation = conf * (freq - 0.5) + 0.5
	
	override def hashCode = {
		val prime = 31
		prime * (prime + freqInt) + confInt
	}
	
	override def equals(that: Any) = that match {
		case other: Truth => freqInt == other.freqInt && confInt == other.confInt
		case _ => false
	}
	
	def closeTo(other: Truth): Boolean =
		(Math.abs(freq - other.freq) <= Truth.EPSILON) && (Math.abs(conf - other.conf) <= Truth.EPSILON)
	
	override def toString = "(%.2f,%.2f)".format(freq, conf)
}