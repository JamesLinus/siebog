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

package siebog.agents.test;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Klasa za testiranje JSON deserializera, kada proradi ConnectionManager obrisati.
 * @author Nikola
 */
public class Foo {
	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		
		String s = "{\"animals\":[{\"type\":\"dog\",\"name\":\"Spike\",\"breed\":\"mutt\",\"leashColor\":\"red\"},{\"type\":\"cat\",\"name\":\"Fluffy\",\"favoriteToy\":\"spider ring\"}]}";
		String a = "{\"type\":\"dog\",\"name\":\"Spike\",\"breed\":\"mutt\",\"leashColor\":\"red\"}";
		
		Zoo zoo = mapper.readValue(s, Zoo.class);
		System.out.println(mapper.writeValueAsString(zoo));
		
		Pets an = mapper.readValue(a, Pets.class);
		System.out.println(mapper.writeValueAsString(an));
		
		Dog d = new Dog();
		d.breed = "a";
		d.leashColor = "b";
		d.name = "c";
		System.out.println(mapper.writeValueAsString(d));
	}
}

class Zoo {
	public Collection<Animal> animals;
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = Cat.class, name = "cat"), @Type(value = Dog.class, name = "dog") })
interface Pets {}

abstract class Animal implements Pets {
	public String name;
}

class Dog extends Animal {
	public String breed;
	public String leashColor;
}

class Cat extends Animal {
	public String favoriteToy;
}