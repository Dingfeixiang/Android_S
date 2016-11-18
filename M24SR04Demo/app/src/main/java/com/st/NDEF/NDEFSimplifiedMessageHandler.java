/*
  * Author					:  MMY Application Team
  * Last committed			:  $Revision: 1616 $
  * Revision of last commit	:  $Rev: 1616 $
  * Date of last commit     :  $Date: 2016-02-03 19:03:03 +0100 (Wed, 03 Feb 2016) $ 
  *
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2015 STMicroelectronics</center></h2>
  *
  * Licensed under ST MYLIBERTY SOFTWARE LICENSE AGREEMENT (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  *        http://www.st.com/myliberty  
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  * AND SPECIFICALLY DISCLAIMING THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************
*/


package com.st.NDEF;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import com.st.NFC.NFCApplication;
import com.xianfeng.m24sr04demo.R;
//import com.st.demo.R;



public class NDEFSimplifiedMessageHandler {
	/**
	 * Defines
	 */
	//Some generic defines


	/**
	 * Attributes
	 */
	private final static Hashtable<String, NDEFSimplifiedMessageType> SupportedNDEFSimpleMsgList =
		new Hashtable<String, NDEFSimplifiedMessageType>() {{
			// NDEF_SIMPLE_MSG_TYPE_EMPTY,
			put(
				NFCApplication.getContext().getResources().getString(R.string.mnf_frag_NDEF_rec_type_empty),
				NDEFSimplifiedMessageType.NDEF_SIMPLE_MSG_TYPE_EMPTY
			);
			// NDEF_SIMPLE_MSG_TYPE_TEXT
			put(
					NFCApplication.getContext().getResources().getString(R.string.mnf_frag_NDEF_rec_type_txt),
					NDEFSimplifiedMessageType.NDEF_SIMPLE_MSG_TYPE_TEXT
				);
			
		}};
	private NDEFSimplifiedMessage _curMessage = null;
	private stnfcndefhandler _ndefHandler = null;

	/**
	 * Methods
	 */
	// Constructor
	public NDEFSimplifiedMessageHandler(stnfcndefhandler mNdefHandler) {
		_ndefHandler = mNdefHandler;
		
		if (_ndefHandler != null) {
			parseNdefMsg();
		}
	}

	// Accessors
	public NDEFSimplifiedMessage            getNDEFSimplifiedMessage()                              { return _curMessage; }
	public static Collection<String>        getSupportedSimpleMsgStrList()                          { return Collections.list(SupportedNDEFSimpleMsgList.keys()); }
	public static NDEFSimplifiedMessageType getMsgTypeFromStr(String msgStr)                        { return SupportedNDEFSimpleMsgList.get(msgStr); }
	public static String                    getStrFromMsgType(NDEFSimplifiedMessageType msgType)    {
		String resStr = null;
		if (SupportedNDEFSimpleMsgList.containsValue(msgType)) {
			for (Map.Entry<String, NDEFSimplifiedMessageType> entry: SupportedNDEFSimpleMsgList.entrySet()) {
				if (msgType == entry.getValue()) {
					resStr = (String) entry.getKey();
					break;
				}
			}
		}

		return resStr;
	}
	public static int                       getMsgPositionInList(NDEFSimplifiedMessageType msgType) { return msgType.ordinal(); }

	// Private methods
	void parseNdefMsg() {
		// Identify the type of the Simplified NDEF message, as per TNF and RTD type
		// (1st record of the NDEF message determines it) 
		// and call the right object constructor
		// TODO: for the moment, only 1 NDEF file supported in this version; to be improved... 
		int unused = 0;
		
		// - Check first if NDEF message is a multiple NDEF Record message
		if (_ndefHandler.getRecordNb()>1)
		{
			_curMessage = new NDEFMultipleRecordMessage();
			_curMessage.setNDEFMessage(null,null, _ndefHandler);
		}
		// - Text message
		else if (NDEFTextMessage.isSimplifiedMessage(_ndefHandler.gettnf(0), _ndefHandler.gettype(0))) {
			_curMessage = new NDEFTextMessage();
			_curMessage.setNDEFMessage(_ndefHandler.gettnf(0), _ndefHandler.gettype(0), _ndefHandler);
		}

		
	}
}
