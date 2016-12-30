/*
 * Copyright 2011-2016 L2EMU UNIQUE
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
package net.l2emuproject.proxy.script.game;

import net.l2emuproject.proxy.script.ScriptFieldAlias;

/**
 * Ability to handle client object selections (clicks on them).
 * 
 * @author _dev_
 */
public interface InteractiveActions
{
	@ScriptFieldAlias
	String TARGET_OID_ACTION = "action_target_oid";
	@ScriptFieldAlias
	String MOVEMENT_PROHIBITED_FOR_ACTION = "action_without_moving";
	@ScriptFieldAlias
	String TARGET_OID_ATTACK = "attack_target_oid";
	@ScriptFieldAlias
	String MOVEMENT_PROHIBITED_FOR_ATTACK = "attack_without_moving";
}
