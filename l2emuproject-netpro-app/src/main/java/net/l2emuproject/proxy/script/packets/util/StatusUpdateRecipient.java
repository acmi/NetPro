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
package net.l2emuproject.proxy.script.packets.util;

import net.l2emuproject.proxy.script.ScriptFieldAlias;

/**
 * Provides access to status update packets and related convenience methods.
 * 
 * @author _dev_
 */
public interface StatusUpdateRecipient
{
	/** This is the runtime ID of the updated object */
	@ScriptFieldAlias
	String SU_UPDATED_ACTOR_OID = "su_actor_oid";
	/** This is the type of an update */
	@ScriptFieldAlias
	String SU_UPDATE_TYPE = "su_update_type";
	/** This is the new value */
	@ScriptFieldAlias
	String SU_UPDATE_VALUE = "su_update_value";
	
	/** Update type: Current HP */
	int SU_TYPE_HP_CURRENT = 9;
	/** Update type: Maximum HP */
	int SU_TYPE_HP_MAXIMUM = 10;
	/** Update type: Current MP */
	int SU_TYPE_MP_CURRENT = 11;
	/** Update type: Maximum MP */
	int SU_TYPE_MP_MAXIMUM = 12;
	/** Update type: Current CP */
	int SU_TYPE_CP_CURRENT = 33;
	/** Update type: Maximum CP */
	int SU_TYPE_CP_MAXIMUM = 34;
}
