/*
 * Copyright 2011-2015 L2EMU UNIQUE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package modifier;

import eu.revengineer.simplejse.HasScriptDependencies;

/**
 * Unmasks the RSA modulus (N) used when deciphering (= testing signature) of client DAT files.
 * 
 * @author _dev_
 */
@HasScriptDependencies("modifier.ClientDatModulus")
public class ClientDatModulus412 extends ClientDatModulus
{
	/** Constructs this modifier. */
	public ClientDatModulus412()
	{
		super(0x30_30_30_30_30_30_32_35L);
	}
}
