/*
 * Copyright 2011-2017 L2EMU UNIQUE
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
package net.l2emuproject.proxy.script.analytics.user;

import java.util.ArrayList;
import java.util.List;

/**
 * @author _dev_
 */
public interface ItemElementalAttributes
{
	int getAtkElement();
	
	int getAtkPower();
	
	int getDefFire();
	
	int getDefWater();
	
	int getDefWind();
	
	int getDefEarth();
	
	int getDefHoly();
	
	int getDefDark();
	
	static List<String> toString(ItemElementalAttributes attributes)
	{
		final List<String> result = new ArrayList<>(3);
		if (attributes.getAtkPower() > 0)
		{
			switch (attributes.getAtkElement())
			{
				case 0:
					result.add("Fire P.Atk. " + attributes.getAtkPower());
					break;
				case 1:
					result.add("Water P.Atk. " + attributes.getAtkPower());
					break;
				case 2:
					result.add("Wind P.Atk. " + attributes.getAtkPower());
					break;
				case 3:
					result.add("Earth P.Atk. " + attributes.getAtkPower());
					break;
				case 4:
					result.add("Holy P.Atk. " + attributes.getAtkPower());
					break;
				case 5:
					result.add("Dark P.Atk. " + attributes.getAtkPower());
					break;
			}
		}
		if (attributes.getDefFire() > 0)
			result.add("Fire P.Def. " + attributes.getDefFire());
		if (attributes.getDefWater() > 0)
			result.add("Water P.Def. " + attributes.getDefWater());
		if (attributes.getDefWind() > 0)
			result.add("Wind P.Def. " + attributes.getDefWind());
		if (attributes.getDefEarth() > 0)
			result.add("Earth P.Def. " + attributes.getDefEarth());
		if (attributes.getDefHoly() > 0)
			result.add("Holy P.Def. " + attributes.getDefHoly());
		if (attributes.getDefDark() > 0)
			result.add("Dark P.Def. " + attributes.getDefDark());
		return result;
	}
	
	ItemElementalAttributes NO_ATTRIBUTES = new ItemElementalAttributes(){
		@Override
		public int getDefWind()
		{
			return 0;
		}
		
		@Override
		public int getDefWater()
		{
			return 0;
		}
		
		@Override
		public int getDefHoly()
		{
			return 0;
		}
		
		@Override
		public int getDefFire()
		{
			return 0;
		}
		
		@Override
		public int getDefEarth()
		{
			return 0;
		}
		
		@Override
		public int getDefDark()
		{
			return 0;
		}
		
		@Override
		public int getAtkPower()
		{
			return 0;
		}
		
		@Override
		public int getAtkElement()
		{
			return -2;
		}
		
		@Override
		public String toString()
		{
			return "No elemental attributes";
		}
	};
}
