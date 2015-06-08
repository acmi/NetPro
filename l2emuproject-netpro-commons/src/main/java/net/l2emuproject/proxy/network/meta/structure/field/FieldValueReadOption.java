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
package net.l2emuproject.proxy.network.meta.structure.field;

/**
 * Enumerates optional actions to perform when reading field values.
 * 
 * @author _dev_
 */
public enum FieldValueReadOption
{
	/** Executes the associated field value modifier (if any) as a postprocessor. */
	APPLY_MODIFICATIONS,
	/** Queries the associated field value interpreter (if any) to get an user-friendly interpretation. */
	COMPUTE_INTERPRETATION;
}
