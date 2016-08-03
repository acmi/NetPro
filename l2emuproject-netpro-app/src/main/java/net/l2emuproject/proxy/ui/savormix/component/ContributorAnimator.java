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
package net.l2emuproject.proxy.ui.savormix.component;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.l2emuproject.util.concurrent.L2ThreadPool;

/**
 * This class provides automated contributor name &amp; comment typing.
 * 
 * @author savormix
 */
public class ContributorAnimator implements Runnable
{
	static final String[] CONTRIBUTORS = { "L2JFree", "L2EMU UNIQUE", };
	static final String[] DESCRIPTORS = { "Initial implementation (High Five)", "http://www.l2emu-unique.net/", };
	int _contributor = 0;
	int _charIndexC = 0;
	int _charIndexD = 0;
	
	final JLabel _name;
	final JLabel _desc;
	
	/**
	 * Creates a pseudoanimation task.
	 * 
	 * @param name left label
	 * @param desc right label
	 */
	public ContributorAnimator(JLabel name, JLabel desc)
	{
		_name = name;
		_desc = desc;
	}
	
	@Override
	public void run()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (_charIndexC < CONTRIBUTORS[_contributor].length())
				_name.setText(CONTRIBUTORS[_contributor].substring(0, ++_charIndexC));
			if (_charIndexD < DESCRIPTORS[_contributor].length())
				_desc.setText(DESCRIPTORS[_contributor].substring(0, ++_charIndexD));
			
			if (_charIndexC < CONTRIBUTORS[_contributor].length() || _charIndexD < DESCRIPTORS[_contributor].length())
				L2ThreadPool.schedule(ContributorAnimator.this, 50);
			else
			{
				// wait and type next contributor
				_charIndexC = _charIndexD = 0;
				_contributor++;
				_contributor %= CONTRIBUTORS.length;
				
				L2ThreadPool.schedule(ContributorAnimator.this, 3_000);
			}
		});
	}
}
