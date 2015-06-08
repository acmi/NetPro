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
package interpreter;

import net.l2emuproject.proxy.script.interpreter.ScriptedZeroBasedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a reason why you could not log in.
 * 
 * @author savormix
 */
public class LoginFailureReason extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public LoginFailureReason()
	{
		// @formatter:off
		super(new InterpreterMetadata(1),
				"There is a system error. Please log in again later.",
				"The password you have entered is incorrect. Confirm your account information and log in again later.",
				"The password you have entered is incorrect. Confirm your account information and log in again later.",
				"Access failed. Please try again later. .",
				"Your account information is incorrect. For more details, please contact our customer service center at http://support.plaync.com.", // 5
				"Access failed. Please try again later. .",
				"Account is already in use. Unable to log in.",
				"Access failed. Please try again later. .",
				"Access failed. Please try again later. .",
				"Access failed. Please try again later. .", // 10
				"Access failed. Please try again later. .",
				null,
				"Access failed. Please try again later. .",
				"Access failed. Please try again later. .",
				"Due to high server traffic, your login attempt has failed. Please try again soon.", // 15
				"Currently undergoing game server maintenance. Please log in again later.",
				"Please login after changing your temporary password.",
				"Your game time has expired. To continue playing, please purchase Lineage II either directly from the PlayNC Store or from any leading games retailer.",
				"There is no time left on this account.",
				"System error.", // 20
				"Access failed.",
				"Game connection attempted through a restricted IP.",
				null,
				null,
				null, // 25
				null,
				null,
				null,
				null,
				"This week's usage time has finished.", // 30
				"Invalid security card number, please input another!",
				"Users who have not verified their age may not log in between the hours of 10:00 p.m. and 6:00 a.m.",
				"This server cannot be accessed by the coupon you are using.",
				null,
				"You are using a computer that does not allow you to log in with two accounts at the same time.", // 35
				null,
				null,
				"A guardian's consent is required before this account can be used to play Lineage II.\nPlease try again after this consent is provided.",
				"This account has declined the User Agreement or is pending a withdrawl request. \nPlease try again after cancelling this request.",
				"This account has been suspended. \nFor more information, please call the Customer's Center (Tel. 1600-0020).", // 40
				null,
				"You are currently logged into 10 of your accounts and can no longer access your other accounts.",
				"The master account of your account has been restricted."
		);
		// @formatter:on
	}
}
