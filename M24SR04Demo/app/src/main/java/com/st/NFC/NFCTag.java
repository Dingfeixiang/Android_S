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

package com.st.NFC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcV;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.st.NDEF.NDEFSimplifiedMessage;
import com.st.NDEF.NDEFSimplifiedMessageHandler;
import com.st.NDEF.stnfcndefhandler;

import com.st.NDEF.stndefwritestatus;
import com.st.NFC.NFCTag.NfcTagTypes;

//import com.st.nfcv.Helper;
//import com.st.nfcv.NFCCommandVLR;
//import com.st.nfcv.stnfcccLRhandler;
//import com.st.nfcv.stnfcm24LRBasicOperation;
//import com.st.nfcv.stnfcccLRhandler.TLVBlockMemory;
//import com.st.nfcv.sysfileLRHandler;
import com.st.util.GenErrorAppReport;
import com.xianfeng.m24sr04demo.R;

public class NFCTag {

	final static String TAG = "NFCTag";

	// to manage the fact that tag tapped is not of the same type
	private static String m_ModelName = "NA";
	private static int m_ModelChanged = 0;

	private stnfcTagHandlerFactory m_stnfcTagHandlerFactory;

	/**
	 * @return the m_ModelChanged
	 */
	public int getM_ModelChanged() {
		return m_ModelChanged;
	}

	/*
	 * Common defines
	 */
	// Tag Type 4 File Control TLV Types (NFCForum-TS-Type-4-Tag_2.0, section
	// 5.1.2)l
	public final static int TAG_TYPE4_CC_FILE_TLV_TYPE_NDEF = 4;
	public final static int TAG_TYPE4_CC_FILE_TLV_TYPE_PROPRIETARY = 5;

	// Constant enums and strings for NFC Tag
	public enum NfcTagTypes {
		NFC_TAG_TYPE_UNKNOWN, NFC_TAG_TYPE_1, NFC_TAG_TYPE_2, NFC_TAG_TYPE_3, NFC_TAG_TYPE_4A, NFC_TAG_TYPE_4B, NFC_TAG_TYPE_A, NFC_TAG_TYPE_B, NFC_TAG_TYPE_F, NFC_TAG_TYPE_V
	}

	private Hashtable<NFCTag.NfcTagTypes, String> TagsTypesDescr = new Hashtable<NFCTag.NfcTagTypes, String>() {
		{
			put(NfcTagTypes.NFC_TAG_TYPE_UNKNOWN, "Unknown tag type");
			put(NfcTagTypes.NFC_TAG_TYPE_1, "NFC Forum Type 1 tag");
			put(NfcTagTypes.NFC_TAG_TYPE_2, "NFC Forum Type 2 tag");
			put(NfcTagTypes.NFC_TAG_TYPE_3, "NFC Forum Type 3 tag");
			put(NfcTagTypes.NFC_TAG_TYPE_4A, "NFC Forum Type 4A tag");
			put(NfcTagTypes.NFC_TAG_TYPE_4B, "NFC Forum Type 4B tag");
			put(NfcTagTypes.NFC_TAG_TYPE_A, "ISO/IEC 14443A / ISO/IEC 18092 tag");
			put(NfcTagTypes.NFC_TAG_TYPE_B, "ISO/IEC 14443B tag");
			put(NfcTagTypes.NFC_TAG_TYPE_F, "FeliCa tag (JIS X6319-4)");
			put(NfcTagTypes.NFC_TAG_TYPE_V, "ISO/IEC 15693 tag");
		}
	};
	// IC Manufacturers codes, as defined in ISO/IEC 7816-6
	public static String[] ICManufacturers = { /* 0x00 */ "Unknown", /* 0x01 */ "Motorola",
			/* 0x02 */ "STMicroelectronics", /* 0x03 */ "Hitachi Ltd", /* 0x04 */ "NXP Semiconductors",
			/* 0x05 */ "Infineon Technologies", /* 0x06 */ "Cylink", /* 0x07 */ "Texas Instruments",
			/* 0x08 */ "Fujitsu Limited", /* 0x09 */ "Matsushita Electronics Corporation", /* 0x0A */ "NEC",
			/* 0x0B */ "Oki Electric Industry Co. Ltd", /* 0x0C */ "Toshiba Corp.",
			/* 0x0D */ "Mitsubishi Electric Corp.", /* 0x0E */ "Samsung Electronics Co. Ltd",
			/* 0x0F */ "Hyundai Electronics Industries Co. Ltd", /* 0x10 */ "LG-Semiconductors Co. Ltd",
			/* 0x11 */ "Emosyn-EM Microelectronics", /* 0x12 */ "Inside Technology",
			/* 0x13 */ "ORGA Kartensysteme GmbH", /* 0x14 */ "SHARP Corporation", /* 0x15 */ "ATMEL",
			/* 0x16 */ "EM Microelectronic-Marin SA", /* 0x17 */ "KSW Microtec GmbH", /* 0x18 */ "Unknown",
			/* 0x19 */ "XICOR, Inc.", /* 0x1A */ "Sony Corporation",
			/* 0x1B */ "Malaysia Microelectronic Solutions Sdn Bhd (MY)", /* 0x1C */ "Emosyn (US)",
			/* 0x1D */ "Shanghai Fudan Microelectronics Co Ltd (CN)", /* 0x1E */ "Magellan Technology Pty Limited (AU)",
			/* 0x1F */ "Melexis NV BO (CH)", /* 0x20 */ "Renesas Technology Corp (JP)", /* 0x21 */ "TAGSYS (FR)",
			/* 0x22 */ "Transcore (US)", /* 0x23 */ "Shanghai Belling Corp Ltd (CN)",
			/* 0x24 */ "Masktech Germany GmbH (DE)", /* 0x25 */ "Innovision Research and Technology",
			/* 0x26 */ "Hitachi ULSI Systems Co Ltd (JP)", /* 0x27 */ "Cypak AB (SE)", /* 0x28 */ "Ricoh (JP)",
			/* 0x29 */ "ASK (FR)", /* 0x2A */ "Unicore Microsystems LLC (RU)",
			/* 0x2B */ "Dallas semiconductor/Maxim (US)", /* 0x2C */ "Impinj Inc (US)",
			/* 0x2D */ "RightPlug Alliance (US)", /* 0x2E */ "Broadcom Corporation (US)",
			/* 0x2F */ "MStar Semiconductor Inc (TW)", /* 0x30 */ "BeeDar Technology Inc (US)",
			/* 0x31 */ "RFIDsec (DK)", /* 0x32 */ "Schweizer Electronic AG (DE)",
			/* 0x33 */ "AMIC Technology Corp (TW)", /* 0x34 */ "Mikron JSC (RU)",
			/* 0x35 */ "Fraunhofer Institute for Photonic Microsystems (DE)", /* 0x36 */ "IDS Microship AG (CH)",
			/* 0x37 */ "Kovio (US)", /* 0x38 */ "AHMT Microelectronic Ltd (CH)",
			/* 0x39 */ "Silicon Craft Technology (TH)", /* 0x3A */ "Advanced Film Device Inc. (JP)",
			/* 0x3B */ "Nitecrest Ltd (UK)", /* 0x3C */ "Verayo Inc. (US)", /* 0x3D */ "HID Global (US)",
			/* 0x3E */ "Productivity Engineering Gmbh (DE)", /* 0x3F */ "AMS (Austria Microsystems)",
			/* 0x40 */ "Gemalto SA (FR)", /* 0x41 */ "Renesas Electronics Corporation (JP)",
			/* 0x42 */ "3Alogics Inc (KR)", /* 0x43 */ "Top TroniQ Asia Limited (Hong Kong)",
			/* 0x44 */ "Gentag Inc (USA)" };

	// Structure to store the products descriptions
	private static class intProductDescr {
		public String _ModelName;
		public String _AddDescr;
		public int _intMemSize;
		public int _intBlckNb;
		public int _intBytesPerBlck;
		public int _Logo; // Drawable
		public int _TranspLogo; // Drawable
		public String _FootNote;
		public NfcMenus[] _MenusList; // menus description

		// Constructor
		public intProductDescr(String mModelName, String mAddDescr, int mMemSize, int mBlckNb, int mBytesPerBlck,
				int mLogo, int mTranspLogo, String mFootNote, NfcMenus[] mMenusList) {
			_ModelName = mModelName;
			_AddDescr = mAddDescr;
			_intMemSize = mMemSize;
			_intBlckNb = mBlckNb;
			_intBytesPerBlck = mBytesPerBlck;
			_Logo = mLogo;
			_TranspLogo = mTranspLogo;
			_FootNote = mFootNote;
			_MenusList = mMenusList;
		}
	}

	// Products internal identifiers
	private enum intProductIDs {
		PRODUCT_UNKNOWN, PRODUCT_UNKNOWN_LR, PRODUCT_ST_UNKNOWN, PRODUCT_ST_LRI64, PRODUCT_ST_LRI512, PRODUCT_ST_LRI1k, PRODUCT_ST_LRI2k, PRODUCT_ST_LRiS2K, PRODUCT_ST_LRiS64k, PRODUCT_ST_M24LR01E_R, PRODUCT_ST_M24LR02E_R, PRODUCT_ST_M24LR04E_R, PRODUCT_ST_M24LR08E_R, PRODUCT_ST_M24LR16E_R, PRODUCT_ST_M24LR32E_R, PRODUCT_ST_M24LR64E_R, PRODUCT_ST_M24LR64_R, PRODUCT_ST_M24LR128E_R, PRODUCT_ST_M24LR256E_R,
		PRODUCT_ST_M24SR02,PRODUCT_ST_M24SR04, PRODUCT_ST_M24SR16, PRODUCT_ST_M24SR64, PRODUCT_ST_T24SR64, PRODUCT_ST_RX95HF, PRODUCT_ST_ST25TA02K, // E2
		PRODUCT_ST_ST25TA02K_P, // A2
		PRODUCT_ST_ST25TA02K_D, // F2
		PRODUCT_ST_ST25TA16K, // C5
		PRODUCT_ST_ST25TA512, // E5
		PRODUCT_ST_ST25TA64K, // C4
		PRODUCT_ST_ST25DV64K, // 26
		PRODUCT_ST_ST25DV04K, // 06
		PRODUCT_ST_SRTAG2KL, PRODUCT_ST_ST95HF,
	}

	// Default menus lists
	private final static NfcMenus[] basicMenusList = new NfcMenus[] { NfcMenus.NFC_MENU_SMART_VIEW_NDEF_FILE,
			NfcMenus.NFC_MENU_NDEF_FILES,
			// NfcMenus.NFC_MENU_CC_FILE,
			NfcMenus.NFC_MENU_TAG_INFO/*
										 * , NfcMenus.NFC_MENU_BIN_FILE
										 */
	};
	// Default menus lists
	private final static NfcMenus[] basicMenusLRList = new NfcMenus[] { NfcMenus.NFC_MENU_SMART_VIEW_NDEF_FILE,
			NfcMenus.NFC_MENU_NDEF_FILES,
			// NfcMenus.NFC_MENU_CC_FILE,
			NfcMenus.NFC_MENU_TAG_INFO,
			NfcMenus.NFC_MENU_M24LR_DEMO/*
										 * , NfcMenus.NFC_MENU_BIN_FILE
										 */
	};

	private final static NfcMenus[] m24lrMenusList = new NfcMenus[] {

			NfcMenus.NFC_MENU_SMART_VIEW_NDEF_FILE, NfcMenus.NFC_MENU_NDEF_FILES, NfcMenus.NFC_MENU_TAG_INFO,
			NfcMenus.NFC_MENU_CC_FILE_LR, NfcMenus.NFC_MENU_SYS_FILE_LR, NfcMenus.NFC_MENU_LR_TOOLS,
			NfcMenus.NFC_MENU_M24LR_DEMO
			/*
			 * , NfcMenus.NFC_MENU_BIN_FILE, NfcMenus.NFC_MENU_M24LR_PWD,
			 * NfcMenus.NFC_MENU_M24LR_LOCK, NfcMenus.NFC_MENU_M24LR_EH
			 */
	};
	private final static NfcMenus[] m24srMenusDemoList = new NfcMenus[] {

			NfcMenus.NFC_MENU_SMART_VIEW_NDEF_FILE, NfcMenus.NFC_MENU_NDEF_FILES, NfcMenus.NFC_MENU_TAG_INFO,
			NfcMenus.NFC_MENU_TOOLS, NfcMenus.NFC_MENU_CC_FILE, NfcMenus.NFC_MENU_SYS_FILE, NfcMenus.NFC_MENU_M24SR_DEMO
			/*
			 * , NfcMenus.NFC_MENU_BIN_FILE, NfcMenus.NFC_MENU_M24SR_PWD,
			 * NfcMenus.NFC_MENU_M24SR_IT
			 */
	};
	private final static NfcMenus[] m24srMenusList = new NfcMenus[] {

			NfcMenus.NFC_MENU_SMART_VIEW_NDEF_FILE, NfcMenus.NFC_MENU_NDEF_FILES, NfcMenus.NFC_MENU_TAG_INFO,
			NfcMenus.NFC_MENU_TOOLS, NfcMenus.NFC_MENU_CC_FILE, NfcMenus.NFC_MENU_SYS_FILE
			/*
			 * , NfcMenus.NFC_MENU_M24SR_DEMO, NfcMenus.NFC_MENU_BIN_FILE,
			 * NfcMenus.NFC_MENU_M24SR_PWD, NfcMenus.NFC_MENU_M24SR_IT
			 */
	};
	private final static NfcMenus[] rx95hfMenusList = new NfcMenus[] { NfcMenus.NFC_MENU_SMART_VIEW_NDEF_FILE,
			NfcMenus.NFC_MENU_NDEF_FILES, NfcMenus.NFC_MENU_TAG_INFO, NfcMenus.NFC_MENU_TOOLS, NfcMenus.NFC_MENU_CC_FILE

	};

	// Products description
	private final static intProductDescr[] intProductsDescr = new intProductDescr[] {
			// PRODUCT_UNKNOWN,
			new intProductDescr("Unknown tag", "", 0, 0, 0, 0, 0, "", basicMenusList/* null */),
			// PRODUCT_UNKNOWN_LR,
			new intProductDescr("Unknown tag", "", 0, 0, 0, 0, 0, "", basicMenusLRList/* null */),
			// PRODUCT_ST_UNKNOWN,
			new intProductDescr("ST tag", "", 0, 0, 0, R.drawable.logo_st_nfc, R.drawable.logo_st_nfc_transp, "",
					basicMenusList),
			// PRODUCT_ST_LRI64,
			new intProductDescr("LRI64", NFCApplication.getContext().getString(R.string.st_tags_add_description), 8, 0,
					0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_LRI512,
			new intProductDescr("LRI512", NFCApplication.getContext().getString(R.string.st_tags_add_description), 64,
					0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_LRI1k,
			new intProductDescr("LRI1k", NFCApplication.getContext().getString(R.string.st_tags_add_description), 128,
					0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_LRI2k,
			new intProductDescr("LRI2k", NFCApplication.getContext().getString(R.string.st_tags_add_description), 256,
					0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_LRiS2K,
			new intProductDescr("LRiS2K", NFCApplication.getContext().getString(R.string.st_tags_add_description), 256,
					0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_LRiS64k,
			new intProductDescr("LRiS64k", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					8192, 2048, 4, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR01E_R,
			new intProductDescr("M24LR01E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					128, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR02E_R,
			new intProductDescr("M24LR02E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					256, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR04E_R,
			new intProductDescr("M24LR04E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					512, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR08E_R,
			new intProductDescr("M24LR08E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					1024, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR16E_R,
			new intProductDescr("M24LR16E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					2048, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR32E_R,
			new intProductDescr("M24LR32E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					4096, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR64E_R,
			new intProductDescr("M24LR64E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					8192, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR64_R,
			new intProductDescr("M24LR64-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					8192, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR128E_R,
			new intProductDescr("M24LR128E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					16384, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24LR256E_R,
			new intProductDescr("M24LR256E-R", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					32768, 0, 0, R.drawable.logo_st_m24lr, R.drawable.logo_st_m24lr_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_M24SR02
			new intProductDescr("M24SR02", NFCApplication.getContext().getString(R.string.st_tags_add_description), 256,
					0, 0, R.drawable.logo_st_m24sr, R.drawable.logo_st_m24sr_transp,
					NFCApplication.getContext().getString(R.string.m24sr_tags_footnote), m24srMenusDemoList),
			// PRODUCT_ST_M24SR04
			new intProductDescr("M24SR04", NFCApplication.getContext().getString(R.string.st_tags_add_description), 512,
					0, 0, R.drawable.logo_st_m24sr, R.drawable.logo_st_m24sr_transp,
					NFCApplication.getContext().getString(R.string.m24sr_tags_footnote), m24srMenusDemoList),
			// PRODUCT_ST_M24SR16
			new intProductDescr("M24SR16", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					2048, 0, 0, R.drawable.logo_st_m24sr, R.drawable.logo_st_m24sr_transp,
					NFCApplication.getContext().getString(R.string.m24sr_tags_footnote), m24srMenusDemoList),
			// PRODUCT_ST_M24SR64
			new intProductDescr("M24SR64", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					8192, 0, 0, R.drawable.logo_st_m24sr, R.drawable.logo_st_m24sr_transp,
					NFCApplication.getContext().getString(R.string.m24sr_tags_footnote), m24srMenusDemoList),
			// PRODUCT_ST_T24SR64
			new intProductDescr("SRTAG64", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					8192, 0, 0, R.drawable.logo_st_m24sr, R.drawable.logo_st_m24sr_transp,
					NFCApplication.getContext().getString(R.string.m24sr_tags_footnote), m24srMenusList),
			// PRODUCT_ST_RX95HF
			new intProductDescr("RX95HF", NFCApplication.getContext().getString(R.string.st_tags_add_description), 0, 0,
					0, R.drawable.logo_st_st95hf, R.drawable.logo_st_st95hf_transp,
					NFCApplication.getContext().getString(R.string.rx95hf_tags_footnote), rx95hfMenusList),
			// PRODUCT_ST_ST25TA2K
			new intProductDescr("ST25TA02K", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					2048, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.ST25TA_tags_footnote), m24srMenusList), // PRODUCT_ST_ST25TA2K-P
			new intProductDescr("ST25TA02K-P", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					2048, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.ST25TA_tags_footnote), m24srMenusList),
			new intProductDescr("ST25TA02K-D", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					2048, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.ST25TA_tags_footnote), m24srMenusList), // PRODUCT_ST_ST25TA16k
			new intProductDescr("ST25TA16k", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					16384, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.ST25TA_tags_footnote), m24srMenusList),
			// PRODUCT_ST_ST25TA512
			new intProductDescr("ST25TA512", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					512, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.ST25TA_tags_footnote), m24srMenusList),
			// PRODUCT_ST_ST25TA64K
			new intProductDescr("ST25TA64K", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					8192, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.ST25TA_tags_footnote), m24srMenusList),
			// PRODUCT_ST_ST25DV64K
			new intProductDescr("ST25DV64K", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					8192, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_ST25DV04K
			new intProductDescr("ST25DV04K", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					512, 0, 0, R.drawable.logo_st25, R.drawable.logo_st25_transp,
					NFCApplication.getContext().getString(R.string.m24lr_tags_footnote), m24lrMenusList),
			// PRODUCT_ST_SRTAG2KL
			new intProductDescr("SRTAG2KL", NFCApplication.getContext().getString(R.string.st_tags_add_description),
					2048, 0, 0, R.drawable.logo_st_m24sr, R.drawable.logo_st_m24sr_transp,
					NFCApplication.getContext().getString(R.string.m24sr_tags_footnote), m24srMenusList),
			// PRODUCT_ST_ST95HF
			new intProductDescr("ST95HF", NFCApplication.getContext().getString(R.string.st_tags_add_description), 0, 0,
					0, R.drawable.logo_st_st95hf, R.drawable.logo_st_st95hf_transp,
					NFCApplication.getContext().getString(R.string.rx95hf_tags_footnote), rx95hfMenusList),
    };



	// Private attributes
	// - tag structures
	private Tag _Tag = null; // android.nfc.Tag

	// private STNfcTagHandler _STTagHandler = null; // only initialized in Type
	// 4
	private stnfcTagGenHandler _STTagHandler = null; // only initialized in Type
														// 4
														// Tag
	// private stnfccchandler _CCHandler = null; // ST NFC CC File Handler
	private CCFileGenHandler _CCHandler = null; // ST NFC CC File Handler
	private stnfcndefhandler _NDEFHandlerArray[]; // ST NFC NDEF File Handler

	private NDEFSimplifiedMessageHandler _NDEFSimplifiedHandlerArray[];
	private SysFileGenHandler _SYSHandler = null; // ST NFC SYS File Handler
	private int _mFileNB;
	private int _mcurrentValidTLVBlockID = -1;
	// private boolean isReadLocked = false; // unusefull
	// private boolean isWriteLocked = false; // unusefull

	private byte[] _BinContent;
	// - product description
	private int[] _UID;
	private String _UIDStr;
	private int _intID;
	public int mProductCode;
	private int _ManufacturerID;
	private NfcTagTypes _Type;
	private int _MemSize;
	private int _BlckNb;
	private int _BytesPerBlck;
	private NdefMessage[] _NdefMsgs = null;

	public boolean tagInvalidate = false;

	// Constructors
	public NFCTag(Tag mTag) {
		_Tag = mTag;
		_NdefMsgs = null;
		tagInvalidate = true;
		_NDEFHandlerArray = new stnfcndefhandler[8];
		_NDEFSimplifiedHandlerArray = new NDEFSimplifiedMessageHandler[8];
		_mFileNB = 0;
		_mcurrentValidTLVBlockID = -1;
		m_stnfcTagHandlerFactory = new stnfcTagHandlerFactory();
		NFCApplication.getApplication().setCurrentTag(this);
		parseTag();
	}

	public NFCTag(Tag mTag, NdefMessage[] mNdefMsgs) {
		_Tag = mTag;
		_NdefMsgs = mNdefMsgs.clone();
		tagInvalidate = true;
		_NDEFHandlerArray = new stnfcndefhandler[8];
		_NDEFSimplifiedHandlerArray = new NDEFSimplifiedMessageHandler[8];
		_mFileNB = 0;
		_mcurrentValidTLVBlockID = -1;
		m_stnfcTagHandlerFactory = new stnfcTagHandlerFactory();
		NFCApplication.getApplication().setCurrentTag(this);
		parseTag();
	}

	// Accessors
	// - General basic Android Tag attributes
	/*
	 * public void setTag(Tag mTag) { _Tag = mTag; _NdefMsgs = null; parseTag();
	 * }
	 */
	public Tag getTag() {
		return _Tag;
	}

	public int[] getUID() {
		return _UID;
	}

	public String getUIDStr() {
		return _UIDStr;
	}

	public NdefMessage[] getNdefMessages() {
		return _NdefMsgs;
	}

	public int getCurrentValideTLVBlokID() {
		return _mcurrentValidTLVBlockID;
	}

	public boolean isaValideTLVBlockIDSelected() {
		return (_mcurrentValidTLVBlockID != -1);
	}

	public void setCurrentValideTLVBlokID(int currentTLVBlockID) {
		_mcurrentValidTLVBlockID = currentTLVBlockID;
	}

	public stnfcTagGenHandler getSTTagHandler() {
		return _STTagHandler;
	}

//	 public boolean isReadLocked() {return this.isReadLocked;} // unusefull
//	 public void setReadLocked(boolean readState) { this.isReadLocked =
//	 readState;} // unusefull
//
//	 public boolean isWriteLocked() {return this.isWriteLocked;} // unusefull
//	 public void setWriteLocked(boolean writeState) { this.isWriteLocked =
//	 writeState;} // unusefull

	// - Product description
	public String getModel() {
		return intProductsDescr[_intID]._ModelName;
	}

	public String getAddDescr() {
		return intProductsDescr[_intID]._AddDescr;
	}

	public int getManufacturerID() {
		return _ManufacturerID;
	}

	public String getManufacturer() {
		return ICManufacturers[_ManufacturerID];
	}

	public int getLogo() {
		return intProductsDescr[_intID]._Logo;
	} /* = Drawable (ResourceId) */

	public int getTranspLogo() {
		return intProductsDescr[_intID]._TranspLogo;
	} /* = Drawable (ResourceId) */

	public String[] getTechList() {
		return _Tag.getTechList();
	}

	public String getFootNote() {
		return intProductsDescr[_intID]._FootNote;
	}

	public NfcMenus[] getMenusList() {
		return intProductsDescr[_intID]._MenusList;
	}

	public NfcTagTypes getType() {
		return _Type;
	}

	public String getTypeStr() {
		return TagsTypesDescr.get(_Type);
	}

	public int getMemSize() {
		return _MemSize;
	}

	public int getBlckNb() {
		return _BlckNb;
	}

	public int getBytesPerBlck() {
		return _BytesPerBlck;
	}

	// - NDEF level
	public CCFileGenHandler getCCHandler() {
		return _CCHandler;
	}
//	 public stnfcndefhandler getNDEFHandler() { return _NDEFHandler; }
//	 public NDEFSimplifiedMessageHandler getNDEFSimplifiedHandler() { return
//	 _NDEFSimplifiedHandler; }

	public short getmaxbytesread() {
		short mmaxbytesread = 246;
		stnfccchandler cchandler = (stnfccchandler) getCCHandler();
		if (cchandler != null) {
			if (cchandler.getmaxbytesread() > 0)
				mmaxbytesread = cchandler.getmaxbytesread();
		}
		return mmaxbytesread;
	}

	public short getmaxbyteswritten() {
		short mmaxbyteswritten = 244;
		stnfccchandler cchandler = (stnfccchandler) getCCHandler();
		if (cchandler != null) {
			cchandler.getmaxbyteswritten();
			if (cchandler.getmaxbyteswritten() > 0)
				mmaxbyteswritten = cchandler.getmaxbyteswritten();
		}
		return mmaxbyteswritten;

	}

	// =======================================================
	// moved from sysfile handler
	public boolean isSRTAG2KL() {
		// return (NFCApplication.getApplication().getCurrentTag().mProductCode
		// == 0xA2);
		if (NFCApplication.getApplication().getCurrentTag().mProductCode == 0xA2)
			return true;
		else
			return false;
	}

	public boolean isST25TA02K() {
		// return (NFCApplication.getApplication().getCurrentTag().mProductCode
		// == 0xA2);
		if ((NFCApplication.getApplication().getCurrentTag().mProductCode == 0xF2)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0xC5)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0xE5)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0xC4)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0xA2)) {
			return true;
		} else
			return false;
	}

	public boolean isM24SR() {
		return ((NFCApplication.getApplication().getCurrentTag().mProductCode == 0x82)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0x86)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0x85)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0x84)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0x8C)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0xC4)
				|| (NFCApplication.getApplication().getCurrentTag().mProductCode == 0x80));
	}

	// =======================================================
	public stnfcndefhandler getNDEFHandler(int TLVID) {
		if (TLVID == -1) {
			Log.e(this.getClass().getName(), " getNDEFHandler with value: " + TLVID);
			return _NDEFHandlerArray[0];
		} else {
			return _NDEFHandlerArray[TLVID];
		}
	}

	public NDEFSimplifiedMessageHandler getNDEFSimplifiedHandler(int TLVID) {
		if (TLVID == -1) {
			Log.e(this.getClass().getName(), " getNDEFSimplifiedHandler with value: " + TLVID);
			return _NDEFSimplifiedHandlerArray[0];
		} else {
			return _NDEFSimplifiedHandlerArray[TLVID];
		}
	}

	public stndefwritestatus writeNDEFMessage(NDEFSimplifiedMessage msgObject) {
		// FBE
		stndefwritestatus writeStatus = stndefwritestatus.WRITE_STATUS_ERR_NOT_SUPPORTED;
		if (_mcurrentValidTLVBlockID == -1) {
			Log.v(this.getClass().getName(),
					"writeNDEFMessage error: pb in NdefMessage retrieval, _mcurrentValidTLVBlockID = -1");
			int fid = (this.getCurrentValideTLVBlokID() ==-1)?0:this.getCurrentValideTLVBlokID();
			writeStatus = writeNDEFMessage(msgObject, fid);
		} else {
			writeStatus = writeNDEFMessage(msgObject, _mcurrentValidTLVBlockID);
		}
		// Update Tab information after write
		// FBE
		if (writeStatus == stndefwritestatus.WRITE_STATUS_OK)
		    decodeTag();

		return writeStatus;
	}

	public stndefwritestatus writeNDEFMessage(NDEFSimplifiedMessage msgObject, int TLVID) {
		stndefwritestatus writeStatus = stndefwritestatus.WRITE_STATUS_ERR_NOT_SUPPORTED;
		String writepassword = "";

		this._mcurrentValidTLVBlockID = TLVID;

		// FBE if (true /*_Type == NfcTagTypes.NFC_TAG_TYPE_4A*/) {
		if (this._Type == NfcTagTypes.NFC_TAG_TYPE_4A) {
			// Retrieve the NDEF message to write
			NdefMessage msgToWrite = msgObject.getNDEFMessage();
			if (msgToWrite == null) {
				Log.v(this.getClass().getName(), "writeNDEFMessage error: pb in NdefMessage retrieval, NULL message");
				writeStatus = stndefwritestatus.WRITE_STATUS_ERR_MALFORMED_STRUCTURE;
			} else {
				// FAR BEGIN
				// ADD Formatable Test if null
				// Test If we have a STM24SR - if yes - use writeTag from ST lib
				// else use native solution

				// Update Object with UI Data if required
				if (this.getNDEFHandler(_mcurrentValidTLVBlockID) == null) // Physical tag not yet handled - then need to create a ndef handler here.
				{
					_NDEFHandlerArray[_mcurrentValidTLVBlockID] = new stnfcndefhandler();

				}
				msgObject.updateSTNDEFMessage(this.getNDEFHandler(_mcurrentValidTLVBlockID));

				if (_NDEFSimplifiedHandlerArray[_mcurrentValidTLVBlockID] == null) {
					_NDEFSimplifiedHandlerArray[_mcurrentValidTLVBlockID] = new NDEFSimplifiedMessageHandler(
							_NDEFHandlerArray[TLVID]);
				}
				stnfccchandler cchandler = (stnfccchandler) this.getCCHandler();
				int evalspace = (cchandler == null ? -1 : cchandler.getndeffilelength(_mcurrentValidTLVBlockID));

				byte[] tmpbuffer = msgToWrite.toByteArray();
				int bufferlength = tmpbuffer.length;
				// int workaroundoldAPI = msgToWrite.getByteArrayLength();
				// if (msgToWrite.getByteArrayLength() >= evalspace) {
				if (bufferlength >= evalspace) {
					String errmessage = "Write NDEF Message issue: " + " Bytes of msg to Write:" + bufferlength
							+ " Available bytes space to Write:" + evalspace;
					Log.v(this.getClass().getName(), errmessage);
					Log.v(this.getClass().getName(), "writNDEFFile error: IO ERR");
					Toast toast = Toast.makeText(NFCApplication.getContext(), errmessage, Toast.LENGTH_LONG);
					toast.show();

					writeStatus = stndefwritestatus.WRITE_STATUS_ERR_IO;
				} else {
					// Check writable access
					boolean tagWritableStatus = true;
					try {
						Ndef ndefTag = Ndef.get(getTag());
						if (ndefTag != null) {
							if (!ndefTag.isConnected())
								ndefTag.connect();
							tagWritableStatus = ndefTag.isWritable();
							ndefTag.close(); // need to close to prevent NFC
							// Technology addressing issue in
							// other activities
						}
					} catch (Exception e) {
						Log.d("TAG", " Writable Check - IO TAG error");
						tagWritableStatus = false;
					}

					// 24SR or SRTAG ......
					if ((this.getModel().contains("24SR") || (this.getModel().contains("SRTAG"))
							|| (this.getModel().contains("ST25TA")))) // ST
					// product
					// -
					// request
					// password
					// and
					// write
					// Tag
					{
						// Put the system in a Select NDEF File Mode.
						if (setInSelectNDEFState(_mcurrentValidTLVBlockID) != 1) {
							_STTagHandler.closeConnection();
							return stndefwritestatus.WRITE_STATUS_ERR_TAG_LOST;
						}

						// if Write Password is required : request it

						if (!_STTagHandler.isNDEFWriteUnLocked()) {
							_STTagHandler.closeConnection();
							return stndefwritestatus.WRITE_STATUS_ERR_PASSWORD_REQUIRED;
						} else if (writeNDEFFile() == 0) {
							Log.v(this.getClass().getName(), "writLockedNDEFFile error: IO ERR");
							writeStatus = stndefwritestatus.WRITE_STATUS_ERR_IO;
						} else {
							writeStatus = stndefwritestatus.WRITE_STATUS_OK;
						}
						_STTagHandler.closeConnection();

						// refresh data
						decodeTagType4A();
					}

					else // Tag is not a ST product
					{
						if (!tagWritableStatus) // if Tag is locked. Can't
												// handle
						// unlock
						{

							writeStatus = stndefwritestatus.WRITE_STATUS_ERR_LOCKED_TAG_NOT_SUPPORTED;
						} else // .. write with NDEF tech API
						{
							Ndef lNdefTag = Ndef.get(_Tag);
							NdefFormatable lNdefFormatable = null;
							if (lNdefTag == null) {
								lNdefFormatable = NdefFormatable.get(_Tag);
							}
							try {
								if ((lNdefTag != null) && (!lNdefTag.isWritable())) {
									Log.v(this.getClass().getName(), "writeNDEFMessage error: tag is read-only");
									writeStatus = stndefwritestatus.WRITE_STATUS_ERR_READ_ONLY_TAG;
								} else if (lNdefTag != null) {
									// Connect to the tag if not already done
									if (!lNdefTag.isConnected()) {
										lNdefTag.connect();
									}
									lNdefTag.writeNdefMessage(msgToWrite);
									lNdefTag.close();

									// Try to refresh the data
									// TODO: manage the return status
									decodeTagType4A();
									_NdefMsgs = new NdefMessage[] { msgToWrite };

									writeStatus = stndefwritestatus.WRITE_STATUS_OK;
								} else if (lNdefFormatable != null) // lNdefTag==null
								{
									lNdefFormatable.connect();
									lNdefFormatable.format(msgToWrite);
									lNdefFormatable.close();
								}
							} catch (TagLostException e) {
								Log.v(this.getClass().getName(),
										"writeNDEFMessage error: tag is no more in the range of the device");
								writeStatus = stndefwritestatus.WRITE_STATUS_ERR_TAG_LOST;
							} catch (IOException e) {
								Log.v(this.getClass().getName(), "writeNDEFMessage error: I/O error on the tag");
								writeStatus = stndefwritestatus.WRITE_STATUS_ERR_IO;
							} catch (FormatException e) {
								Log.v(this.getClass().getName(), "writeNDEFMessage error: malformed NdefMessage");
								writeStatus = stndefwritestatus.WRITE_STATUS_ERR_MALFORMED_STRUCTURE;
							}
						}
					}
				}
			}
			// Log.v(this.getClass().getName(), "writeNDEFMessage: Write status
			// is: " + writeStatus);
		}
		return writeStatus;
	}

	// - BIN level
	public byte[] getBinContent() {
		return _BinContent;
	}

	// - ST products specific features
	public SysFileGenHandler getSYSHandler() {
		return _SYSHandler;
	}

	// Comparator
	@Override
	public boolean equals(Object inTag) {
		if (this == inTag) {
			return true;
		}

		if (!(inTag instanceof NFCTag)) {
			return false;
		}

		NFCTag cmpTag = (NFCTag) inTag;
		if ((this._UIDStr.equals(cmpTag.getUIDStr()))
		/* && (this.toString().equals(cmpTag.toString())) */) {
			return true;
		}

		return false;
	}

	private void parseTag() {
		// Identify the type of the tag, as per the Digital Specification of NFC
		// Forum,
		// from the tech list given by Android in the tag
		_Type = decodeTagType(_Tag);

		// Decode the ID, as per the tag type
		decodeTagID(_Tag.getId());

		// Associate an internal tag ID, in case tag can be identified as a
		// known product
		identifyProduct();

		// Fill in some internal variables
		_MemSize = intProductsDescr[_intID]._intMemSize;
		_BlckNb = intProductsDescr[_intID]._intBlckNb;
		_BytesPerBlck = intProductsDescr[_intID]._intBytesPerBlck;

		if (NFCTag.m_ModelName == "NA") {
			NFCTag.m_ModelChanged = 0;
			NFCTag.m_ModelName = this.getModel();
		}
		if (NFCTag.m_ModelName != "NA" && NFCTag.m_ModelName != this.getModel()) {
			NFCTag.m_ModelChanged = 1;
			NFCTag.m_ModelName = this.getModel();
		} else {
			NFCTag.m_ModelChanged = 0;
			NFCTag.m_ModelName = this.getModel();
		}
		// FBE
		decodeTag();
		// Specific treatment for each tag type
		/*
		 * if (_Type == NfcTagTypes.NFC_TAG_TYPE_4A) { decodeTagType4A(); }
		 */
	}

	/*
	 * Function to identify the tag type, as defined in NFC Forum Digital
	 * Protocol specification, section 2, Table 3; this is identified from the
	 * tech list of the tag (list of Strings)
	 */
	private static NfcTagTypes decodeTagType(Tag pTag) {
		NfcTagTypes lType = NfcTagTypes.NFC_TAG_TYPE_UNKNOWN;
		List<String> lTechList = Arrays.asList(pTag.getTechList());
		String nfcTechPrefixStr = "android.nfc.tech.";
		// Try the Ndef technology
		Ndef lNdefTag = Ndef.get(pTag);
		if (lNdefTag != null) {
			if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_1)) {
				lType = NfcTagTypes.NFC_TAG_TYPE_1;
			} else if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_2)) {
				lType = NfcTagTypes.NFC_TAG_TYPE_2;
			} else if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_3)) {
				lType = NfcTagTypes.NFC_TAG_TYPE_3;
			} else if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_4)) {
				if (lTechList.contains(nfcTechPrefixStr + "NfcA")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_4A;
				} else if (lTechList.contains(nfcTechPrefixStr + "NfcB")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_4B;
				}
			} else if (lTechList.contains(nfcTechPrefixStr + "NfcV")) {
				lType = NfcTagTypes.NFC_TAG_TYPE_V;
			}
		} else {
			// Try the IsoDep technology
			IsoDep lIsoDepTag = IsoDep.get(pTag);
			if (lIsoDepTag != null) {
				if (lTechList.contains(nfcTechPrefixStr + "NfcA")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_4A;
				} else if (lTechList.contains(nfcTechPrefixStr + "NfcB")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_4B;
				}
			} else {
				// Try the underlying technologies
				if (lTechList.contains(nfcTechPrefixStr + "NfcA")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_A;
				} else if (lTechList.contains(nfcTechPrefixStr + "NfcB")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_B;
				} else if (lTechList.contains(nfcTechPrefixStr + "NfcF")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_F;
				} else if (lTechList.contains(nfcTechPrefixStr + "NfcV")) {
					lType = NfcTagTypes.NFC_TAG_TYPE_V;
				}
			}
		}

		return lType;
	}

	/*
	 * Function to decode the tag ID according to the tag type This is currently
	 * done in this function, but should be done later done in the libraries of
	 * each techno
	 */
	private void decodeTagID(byte[] pTagID) {
		/*
		 * WARNING: Java "byte" is signed -> need to cast it to an "int" with
		 * "0xFF" mask, to get the hex value
		 */
		_UID = new int[pTagID.length];
		char[] mtmpStr = new char[(_UID.length * 3) - 1];
		mProductCode = 0; // default value

		switch (_Type) {
		case NFC_TAG_TYPE_V:
			// ISO/IEC 15693-3 section 4.1 defines UID and its components
			// No info found on the byte order of the 10 bytes...
			// Seems UID is given in reverse byte order... at least for ST
			// tags...
			for (int i = 0; i < pTagID.length; i++) {
				_UID[i] = (pTagID[pTagID.length - 1 - i] & 0xFF);
				mtmpStr[i * 3] = Character.forDigit(((pTagID[pTagID.length - 1 - i] & 0x000000F0) >> 4), 16);
				mtmpStr[i * 3 + 1] = Character.forDigit((pTagID[pTagID.length - 1 - i] & 0x0000000F), 16);
				if (i < (pTagID.length - 1)) {
					mtmpStr[i * 3 + 2] = ' ';
				}
			}
			_ManufacturerID = _UID[1];
			// default value for menu ....... for LR
			mProductCode = 1;
			if (_ManufacturerID == 0x02) {
				mProductCode = _UID[2];
			}
			break;
		case NFC_TAG_TYPE_4A:
			// ISO/IEC 14443 type A defines UID in section 6.5.4:
			// - single size UID (4 bytes) begins with 0x08 for random generated
			// number,
			// some other values are proprietary ones, others are RFU
			// - double (7 bytes) and triple size UIDs begin with 1 byte for
			// Manufacturer ID
			for (int i = 0; i < pTagID.length; i++) {
				_UID[i] = (pTagID[i] & 0xFF);
				mtmpStr[i * 3] = Character.forDigit(((pTagID[i] & 0x000000F0) >> 4), 16);
				mtmpStr[i * 3 + 1] = Character.forDigit((pTagID[i] & 0x0000000F), 16);
				if (i < (pTagID.length - 1)) {
					mtmpStr[i * 3 + 2] = ' ';
				}
			}
			if (_UID.length >= 7) {
				_ManufacturerID = _UID[0];
				if (_ManufacturerID == 0x02) {
					mProductCode = _UID[1];
				}
			} else {
				_ManufacturerID = 0;
			}
			break;
		// Other tags are not supported for the moment
		// IC Manufacturer ID cannot be derived from an ISO/IEC 18092 tag, as
		// NFCID1 and NFCID2 are not standardized that way
		case NFC_TAG_TYPE_1:
		case NFC_TAG_TYPE_2:
		case NFC_TAG_TYPE_3:
		case NFC_TAG_TYPE_4B:
		case NFC_TAG_TYPE_A:
			// Same for ISO/IEC 14443 type B tags, which PUPI is randomly
			// generated (ISO/IEC 14443-3, section 7.9.2);
			// we also don't know if the ID given by Android is PUPI or assigned
			// CID...
		case NFC_TAG_TYPE_B:
			// For JIS X6319-4, NFCID2 is defined in section 7.6.2:
			// - 8 bytes long
			// - 2 first bytes compose IC manufacturer code... but this code is
			// not defined in this spec...
		case NFC_TAG_TYPE_F:
		default:
			for (int i = 0; i < pTagID.length; i++) {
				_UID[i] = (pTagID[i] & 0xFF);
				mtmpStr[i * 3] = Character.forDigit(((pTagID[i] & 0x000000F0) >> 4), 16);
				mtmpStr[i * 3 + 1] = Character.forDigit((pTagID[i] & 0x0000000F), 16);
				if (i < (pTagID.length - 1)) {
					mtmpStr[i * 3 + 2] = ' ';
				}
			}
			_ManufacturerID = 0;
			break;
		}

		/* Build the associated UID string */
		_UIDStr = new String(mtmpStr);
		_UIDStr = _UIDStr.toUpperCase(Locale.US);

		// Prevent from any potential problem in case value has been badly read
		if (_ManufacturerID > ICManufacturers.length) {
			_ManufacturerID = 0;
		}
	}

	/*
	 * From Manufacturer and UID, try to identify the product implementing the
	 * tag
	 */
	private void identifyProduct() {
		// Set to "Unknown" by default, to avoid adding it in all "else" cases
		_intID = intProductIDs.PRODUCT_UNKNOWN.ordinal();

		// Init based on TAG type
		if (_Type == NfcTagTypes.NFC_TAG_TYPE_V) _intID = intProductIDs.PRODUCT_UNKNOWN_LR.ordinal();
		if (_Type == NfcTagTypes.NFC_TAG_TYPE_4A) _intID = intProductIDs.PRODUCT_UNKNOWN.ordinal();

		if (_ManufacturerID == 0x02) {
			// Set to "ST tag" by default, to avoid adding it in all "else"
			// cases
			_intID = intProductIDs.PRODUCT_ST_UNKNOWN.ordinal();

			// ISO15693 products (M24LRxx & LRIxx)
			if (_Type == NfcTagTypes.NFC_TAG_TYPE_V) {
				if ((mProductCode & 0xFC) == 0x14) {
					_intID = intProductIDs.PRODUCT_ST_LRI64.ordinal();
				} else if ((mProductCode & 0xFC) == 0x00) { // 0x04 ???
					_intID = intProductIDs.PRODUCT_ST_LRI512.ordinal();
				} else if ((mProductCode & 0xFC) == 0x40) {
					_intID = intProductIDs.PRODUCT_ST_LRI1k.ordinal();
				} else if ((mProductCode & 0xFC) == 0x20) {
					_intID = intProductIDs.PRODUCT_ST_LRI2k.ordinal();
				} else if ((mProductCode & 0xFC) == 0x28) {
					_intID = intProductIDs.PRODUCT_ST_LRiS2K.ordinal();
				} else if ((mProductCode & 0xFC) == 0x44) {
					_intID = intProductIDs.PRODUCT_ST_LRiS64k.ordinal();
				} else if ((mProductCode & 0xFC) == 0x48) {
					_intID = intProductIDs.PRODUCT_ST_M24LR01E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x50) {
					_intID = intProductIDs.PRODUCT_ST_M24LR02E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x58) {
					_intID = intProductIDs.PRODUCT_ST_M24LR04E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x60) {
					_intID = intProductIDs.PRODUCT_ST_M24LR08E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x4C) {
					_intID = intProductIDs.PRODUCT_ST_M24LR16E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x54) {
					_intID = intProductIDs.PRODUCT_ST_M24LR32E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x5C) {
					_intID = intProductIDs.PRODUCT_ST_M24LR64E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x2C) {
					_intID = intProductIDs.PRODUCT_ST_M24LR64_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x64) {
					_intID = intProductIDs.PRODUCT_ST_M24LR128E_R.ordinal();
				} else if ((mProductCode & 0xFC) == 0x6C) {
					_intID = intProductIDs.PRODUCT_ST_M24LR256E_R.ordinal();
				}else if ((mProductCode) == 0x26) {
					_intID = intProductIDs.PRODUCT_ST_ST25DV64K.ordinal();
				}else if ((mProductCode) == 0x06) {
					_intID = intProductIDs.PRODUCT_ST_ST25DV04K.ordinal();
				} else {
					_intID = intProductIDs.PRODUCT_UNKNOWN_LR.ordinal();
				}
			}
			// ISO14443A products (M24sRxx, T24SRxx & RX95HF, ST25TAxx)
			else if (_Type == NfcTagTypes.NFC_TAG_TYPE_4A) {
				if (mProductCode == 0x82) {
					_intID = intProductIDs.PRODUCT_ST_M24SR02.ordinal();
				} else if (mProductCode == 0x86) {
					_intID = intProductIDs.PRODUCT_ST_M24SR04.ordinal();
				} else if (mProductCode == 0x85) {
					_intID = intProductIDs.PRODUCT_ST_M24SR16.ordinal();
				} else if ((mProductCode == 0x84) || (mProductCode == 0x8C)) {
					_intID = intProductIDs.PRODUCT_ST_M24SR64.ordinal();
				} else if (mProductCode == 0x80) {
					_intID = intProductIDs.PRODUCT_ST_RX95HF.ordinal();
				} else if (mProductCode == 0xE2) {
					_intID = intProductIDs.PRODUCT_ST_ST25TA02K.ordinal();
				} else if (mProductCode == 0xA2) {
					_intID = intProductIDs.PRODUCT_ST_ST25TA02K_P.ordinal();
				} else if (mProductCode == 0xF2) {
					_intID = intProductIDs.PRODUCT_ST_ST25TA02K_D.ordinal();
				} else if (mProductCode == 0xC5) {
					_intID = intProductIDs.PRODUCT_ST_ST25TA16K.ordinal();
				} else if (mProductCode == 0xE5) {
					_intID = intProductIDs.PRODUCT_ST_ST25TA512.ordinal();
				} else if (mProductCode == 0xC4) {
					_intID = intProductIDs.PRODUCT_ST_ST25TA64K.ordinal();
				} // OLD products replaced ...
				else if (mProductCode == 0xC4) {
					_intID = intProductIDs.PRODUCT_ST_T24SR64.ordinal();
				} else if (mProductCode == 0xA2) {
					_intID = intProductIDs.PRODUCT_ST_SRTAG2KL.ordinal();
				}
			}
		}
	}

	/**
	 * @return
	 */
	public int decodeTag() {
		final int SUCCESS_RES = 1;
		int retRes = SUCCESS_RES;
		if (this.getType() == NfcTagTypes.NFC_TAG_TYPE_4A) {
			retRes = decodeTagType4A();
		}
		/*
		 * else if (this.getType() == NfcTagTypes.NFC_TAG_TYPE_2 ||
		 * this.getType() == NfcTagTypes.NFC_TAG_TYPE_V) { retRes =
		 * decodeTagType2(); }
		 */
		else if (this.getType() == NfcTagTypes.NFC_TAG_TYPE_2) {
//			retRes = decodeTagType2();
		} else if (this.getType() == NfcTagTypes.NFC_TAG_TYPE_V) {
//			retRes = decodeTagTypeV();
		}

		return retRes;
	}

	/*
	 * Decoding functions for each type of tag
	 */
    private int decodeTagType4A() {
		// Initialize parsing structure (STNfcTagHandler from ST NFC Lib)
		final int SUCCESS_RES = 1;
		int retRes = SUCCESS_RES;

		// _STTagHandler = new STNfcTagHandler(_Tag);
		_STTagHandler = (STNfcTagHandler) m_stnfcTagHandlerFactory.getTagHandler("STNfcTagHandler", _Tag,
				this.getModel());
		try {
			// Application Select
			GenErrorAppReport appret = _STTagHandler.SelectCommand();
			retRes = appret.m_err_value;
		} catch (Exception e) {
			// If an error occurred in any of these operations, log it, and
			// remove the data
			Log.v(this.getClass().getName(),
					"Error in decodeTagType4A: Cannot select NDEF Tag application : " + e.toString());
			// FBE Exception raised but return value not updated
			retRes = 0;
		}

		if (retRes != SUCCESS_RES) {
			Log.v(this.getClass().getName(), "Error in decodeTagType4A: application selection failed");
		} else {
			try {
				// Get CC File
				// - Select CC file
				retRes = _STTagHandler.requestCCSelect();
				if (retRes != SUCCESS_RES) {
					Log.v(this.getClass().getName(), "Error in decodeTagType4A: Cannot select CC file");
					retRes = 0;
				} else {
					// - Read CC file length
					int CCLength = _STTagHandler.requestCCReadLength();
					if (CCLength == 0) {
						Log.v(this.getClass().getName(), "Error in decodeTagType4A: CC file length is null");
						retRes = 0;
					} else {
						// - Read CC content
						byte[] CCBinary = new byte[CCLength];
						retRes = _STTagHandler.requestCCRead(CCLength, CCBinary);
						if (retRes != SUCCESS_RES) {
							Log.v(this.getClass().getName(),
									"Error in decodeTagType4A: Failed to read CC file content");
							retRes = 0;
						} else {
							_CCHandler = new stnfccchandler(CCBinary);
							retRes = SUCCESS_RES;
							// TBD - Lock must be managed by File not by CCFile
							this._mFileNB = ((stnfccchandler) (_CCHandler)).getnbNDEFFile();
							for (int i = 0; i < _mFileNB; i++) {
								GenErrorAppReport appret = _STTagHandler
										.selectNdef(((stnfccchandler) (_CCHandler)).getfieldId(i));
								retRes = appret.m_err_value;
								if (retRes != SUCCESS_RES) {
									Log.v(this.getClass().getName(),
											"Error in decodeTagType4A: Cannot select NDEF file for access check");
									retRes = SUCCESS_RES;
								} else {
									// Patch for new Pwd management Only needed
									// for ST25TA version 0x20 read in Sysfile
									// use verify cmd to know Read/Write status
									boolean locked = false;
									stnfcProtectionLockStates _lockstate = stnfcProtectionLockStates.NDEF_LOCK_NO;
									locked = _STTagHandler.isNDEFReadUnLocked(); // ==>
																					// cmd
																					// verify
																					// ....
									_lockstate = ((STNfcTagHandler) _STTagHandler).m_Type4TagOperationM24SR
											.get_mProtectionLockMgt().getlastProtectionstate();
									((stnfccchandler) (_CCHandler)).setProtectionRLockState(i,
											_lockstate == stnfcProtectionLockStates.NDEF_LOCK_NO ? false : true,
											_lockstate);
									// _CCHandler.setextreadaccessenabled(i,locked?false:true);
									_lockstate = stnfcProtectionLockStates.NDEF_LOCK_NO;
									locked = _STTagHandler.isNDEFWriteUnLocked();// ==>
																					// cmd
																					// verify
																					// ....
									_lockstate = ((STNfcTagHandler) _STTagHandler).m_Type4TagOperationM24SR
											.get_mProtectionLockMgt().getlastProtectionstate();
									((stnfccchandler) (_CCHandler)).setProtectionWLockState(i,
											_lockstate == stnfcProtectionLockStates.NDEF_LOCK_NO ? false : true,
											_lockstate);
									// _CCHandler.setextwriteaccessenabled(i,locked?false:true);
									// End patch
								}
							}
							retRes = SUCCESS_RES;

						}
					}
				}
			} catch (Exception e) {
				// If an error occurred in any of these operations, log it, and
				// remove the data
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: Exception when accessing CC file");
				_CCHandler = null;
				retRes = 0;
			}
			try {
				// Get NDEF File
				// This is possible only if we could get the CC file...
				// TBD - Introduce lock management - ||
				// (_CCHandler.isNDEFLOCKRead() ) ||
				// (_CCHandler.isNDEFPermanentLOCKRead() ))
				if (_CCHandler == null || retRes != SUCCESS_RES) {
					Log.v(this.getClass().getName(),
							"Error in decodeTagType4A: Cannot read NDEF file (missing CC File)");
					// _NDEFHandler = new stnfcndefhandler();
					for (int i = 0; i < 8; i++) {
						_NDEFHandlerArray[i] = null; // No NDEF Handler has MSG
														// is locked in Read
														// Mode
						_NDEFSimplifiedHandlerArray[i] = null;
					}
				} else {
					// - Select NDEF File
					this._mFileNB = ((stnfccchandler) (_CCHandler)).getnbNDEFFile();
					for (int i = 0; i < _mFileNB; i++) {
						if ((((stnfccchandler) (_CCHandler)).isNDEFLOCKRead(i)
								|| ((stnfccchandler) (_CCHandler)).isNDEFPermanentLOCKRead(i))) {
							_NDEFHandlerArray[i] = null;
							_NDEFSimplifiedHandlerArray[i] = null;
						} else {
							// retRes =
							// _STTagHandler.selectNdef(_CCHandler.getfieldId(i));
							GenErrorAppReport appret = _STTagHandler
									.selectNdef(((stnfccchandler) (_CCHandler)).getfieldId(i));
							retRes = appret.m_err_value;

							if (retRes != SUCCESS_RES) {
								Log.v(this.getClass().getName(), "Error in decodeTagType4A: Cannot select NDEF file");
								retRes = SUCCESS_RES;
							} else {
								// - Read NDEF File length
								int NDEFLength = _STTagHandler.readNdeflength();
								if (NDEFLength == 0) {
									Log.v(this.getClass().getName(),
											"Error in decodeTagType4A: NDEF file length is null");
									retRes = SUCCESS_RES;
								} else {
									// - Read NDEF content
									// Ndef Binary file object is composed with
									// 2 NLENGHT Bytes + 1 NDEF Message.
									// As consequence we need to consider those
									// 2 Bytes to get the full NDEF File.
									byte[] NDEFBinary = new byte[NDEFLength + 2]; // Size
																					// stored
																					// in
																					// first
																					// 2
																					// bytes
																					// doesn't
																					// take
																					// into
																					// account
																					// the
																					// first
																					// 2
																					// bytes
									retRes = _STTagHandler.readNdefBinary(NDEFBinary);
									if (retRes != SUCCESS_RES) {
										Log.v(this.getClass().getName(),
												"Error in decodeTagType4A: Failed to read NDEF file content");
										retRes = SUCCESS_RES;
									} else {
										_NDEFHandlerArray[i] = new stnfcndefhandler(NDEFBinary);
										// Put the NDEF message in a Simplified
										// NDEF message
										_NDEFSimplifiedHandlerArray[i] = new NDEFSimplifiedMessageHandler(
												_NDEFHandlerArray[i]);
									}
								}
							}
						}
					}
				}

				int previousFileID = NFCApplication.getApplication().getFileID();
				if (previousFileID == -1)
					_mcurrentValidTLVBlockID = 0;
				else if (previousFileID > this._mFileNB)
					_mcurrentValidTLVBlockID = 0;
				else
					_mcurrentValidTLVBlockID = previousFileID;
				NFCApplication.getApplication().setFileID(_mcurrentValidTLVBlockID);
			} catch (Exception e) {
				// If an error occurred in any of these operations, log it, and
				// remove the data
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: Exception when accessing NDEF file");
				for (int i = 0; i < 8; i++) {
					_NDEFHandlerArray[i] = null;
					_NDEFSimplifiedHandlerArray[i] = null;
				}
				retRes = 0;
			}

			try {
				// Check if SYSTEM file is expected for current tag
				// (NFC_MENU_SYS_FILE is part of the menus list)
				if (getModel().contains("24SR") || getModel().contains("SRTAG") || getModel().contains("ST25TA")) {
					// Get SYSTEM File
					// - Select SYSTEM file
					retRes = _STTagHandler.requestSysSelect();
					if (retRes != SUCCESS_RES) {
						Log.v(this.getClass().getName(), "Error in decodeTagType4A: Cannot select SYSTEM file");
					} else {
						// - Read SYSTEM file length
						int SYSLength = _STTagHandler.requestSysReadLength();
						if (SYSLength == 0) {
							Log.v(this.getClass().getName(), "Error in decodeTagType4A: SYSTEM file length is null");
						} else {
							// - Read SYSTEM content
							byte[] SYSBinary = new byte[SYSLength];
							retRes = _STTagHandler.requestSysRead(SYSLength, SYSBinary);
							if (retRes != SUCCESS_RES) {
								Log.v(this.getClass().getName(),
										"Error in decodeTagType4A: Failed to read SYSTEM file content");
							} else {
								_SYSHandler = new sysfileHandler(SYSBinary);

								// Update the memory size with the value read in
								// SYS file
								_MemSize = ((sysfileHandler) (_SYSHandler)).getmemorySize() + 1; // Value
								// is
								// stored
								// in
								// bits
								retRes = SUCCESS_RES;
							}
						}
					}
					// End the loop here
				}
			} catch (Exception e) {
				// If an error occurred in any of these operations, log it, and
				// remove the data
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: Exception when accessing SYS file");
				_SYSHandler = null;
				retRes = 0;
			}
			// Close current connection
			try {
				_STTagHandler.closeConnection();

			} catch (Exception e) {
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: Exception when closing Tag Handler");
			}

		}
		return retRes;
	}

	private boolean isASystemFileTag() {
		// Identify the type of the tag, as per the Digital Specification of NFC
		// Forum,
		// from the tech list given by Android in the tag
		Boolean result = false;
		if (_Tag != null) {
			_Type = decodeTagType(_Tag);
			// Decode the ID, as per the tag type
			decodeTagID(_Tag.getId());
			// Associate an internal tag ID, in case tag can be identified as a
			// known product
			identifyProduct();
			for (NfcMenus menu : intProductsDescr[_intID]._MenusList) {
				if (menu == NfcMenus.NFC_MENU_SYS_FILE) {
					result = true;
				}
			}
		}
		return result;
	}

	// default writeNDEF file in First File - kept for backward compatibility
	public int writeNDEFFile() {
		if (_mcurrentValidTLVBlockID == -1)
			_mcurrentValidTLVBlockID = 0; // use by default first file
		return writeNDEFFile(_mcurrentValidTLVBlockID);
	}

	public int writeNDEFFile(int TLVBlockID) {
		final int SUCCESS_RES = 1;
		int ret = SUCCESS_RES;

		_mcurrentValidTLVBlockID = TLVBlockID;
		// Select NDEF File
		if (setInSelectNDEFState(_mcurrentValidTLVBlockID) == 0) {
			Log.d("TAG", "writLockedNDEFFile Failed to select NDEF File");
			return 0;
		}
		// Now we can write the NDEF File.
		// Serialize NDEF file from

		if ((_NDEFHandlerArray[_mcurrentValidTLVBlockID] == null)
				|| (!_STTagHandler.updateBinary(_NDEFHandlerArray[_mcurrentValidTLVBlockID].serialize()))) {
			Log.v(this.getClass().getName(), "Error in writLockedNDEFFile: Failed to update binary");
			ret = 0;
		}
		;
		return ret;

	}

	public int readLockedNDEFFile(String password) {
		final int SUCCESS_RES = 1;
		if (_mcurrentValidTLVBlockID == -1) {
			// _mcurrentValidTLVBlockID =-1;
			return 0;

		}
		int ret = setInSelectNDEFState(_mcurrentValidTLVBlockID);

		String tmppassword = password;
		if (tmppassword.length() % 2 != 0)
			tmppassword = tmppassword + "F";

		byte[] unlockPassword128bitLong = null;

		unlockPassword128bitLong = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

		System.arraycopy(stnfchelper.hexStringToByteArray(tmppassword), 0, unlockPassword128bitLong, 0,
				tmppassword.length() / 2);
		if (_STTagHandler.isNDEFReadUnLocked(unlockPassword128bitLong)) {
			// NDEF buffer is considered as accesibles
			// - Read NDEF File length
			int NDEFLength = _STTagHandler.readNdeflength();
			// if (NDEFLength == 0) {
			// Log.v(this.getClass().getName(), "Error in decodeTagType4A: NDEF
			// file length is null");
			// ret = 0;
			// } else {
			// - Read NDEF content
			// Ndef Binary file object is composed with 2 NLENGHT Bytes + 1
			// NDEF Message.
			// As consequence we need to consider those 2 Bytes to get the
			// full NDEF File.
			byte[] NDEFBinary = new byte[NDEFLength + 2];
			ret = _STTagHandler.readNdefBinary(NDEFBinary);
			if (ret != SUCCESS_RES) {
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: Failed to read NDEF file content");
				ret = 0;
			} else {
				// - Check if NDEF data are present for current tag
				_NDEFHandlerArray[_mcurrentValidTLVBlockID] = new stnfcndefhandler(NDEFBinary);
				// Put the NDEF message in a Simplified NDEF message
				_NDEFSimplifiedHandlerArray[_mcurrentValidTLVBlockID] = new NDEFSimplifiedMessageHandler(
						_NDEFHandlerArray[_mcurrentValidTLVBlockID]);
			}
			// }
		} else {
			ret = 0;
		}
		_STTagHandler.closeConnection();
		return ret;
	}

	public int setInSelectSysFileState() {
		// Initialize parsing structure (STNfcTagHandler from ST NFC Lib)
		if (_STTagHandler == null) {
			// _STTagHandler = new STNfcTagHandler(_Tag);
			_STTagHandler = m_stnfcTagHandlerFactory.getTagHandler("STNfcTagHandler", _Tag, this.getModel());
		}

		final int SUCCESS_RES = 1;
		int retRes = SUCCESS_RES;

		try {
			// Application Select
			GenErrorAppReport appret = _STTagHandler.SelectCommand();
			retRes = appret.m_err_value;
			// retRes = _STTagHandler.requestATS();
		} catch (Exception e) {
			// If an error occurred in any of these operations, log it, and
			// remove the data
			Log.v(this.getClass().getName(), "IO Exceotion - Cannot select NDEF Tag application");
		}
		if (retRes != SUCCESS_RES) {
			Log.v(this.getClass().getName(), "Error in decodeTagType4A: Cannot select NDEF Tag application");
		} else {
			try {
				retRes = _STTagHandler.requestSysSelect();
			} catch (Exception e) {
				// If an error occurred in any of these operations, log it, and
				// remove the data
				Log.v(this.getClass().getName(), "IO Exceotion in decodeTagType4A: Cannot select SYS file");
			}
			if (retRes != SUCCESS_RES) {
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: annot select SYS file");
			}
		}
		return retRes;
	}

	public int setInselectNDEFState() {
		// By default get first File ID from the first TLVBlock
		return setInSelectNDEFState(0);

	}

	public int setInSelectNDEFState(int TLVBlockID) {
		// Initialize parsing structure (STNfcTagHandler from ST NFC Lib)
		if (_STTagHandler == null) {
			// _STTagHandler = new STNfcTagHandler(_Tag);
			_STTagHandler = m_stnfcTagHandlerFactory.getTagHandler("STNfcTagHandler", _Tag, this.getModel());
		}

		final int SUCCESS_RES = 1;
		int retRes = SUCCESS_RES;

		try {
			// Application Select
			GenErrorAppReport appret = _STTagHandler.SelectCommand();
			retRes = appret.m_err_value;
			// retRes = _STTagHandler.requestATS();
		} catch (Exception e) {
			// If an error occurred in any of these operations, log it, and
			// remove the data
			Log.v(this.getClass().getName(), "Error in decodeTagType4A: Cannot select NDEF Tag application");
		}

		if (retRes != SUCCESS_RES) {
			Log.v(this.getClass().getName(), "Error in decodeTagType4A: application selection failed");
		} else {
			try {
				// Get CC File
				// - Select CC file
				retRes = _STTagHandler.requestCCSelect();
				if (retRes != SUCCESS_RES) {
					Log.v(this.getClass().getName(), "Error in decodeTagType4A: Cannot select CC file");
					retRes = SUCCESS_RES;
				} else {
					// - Read CC file length
					int CCLength = _STTagHandler.requestCCReadLength();
					if (CCLength == 0) {
						Log.v(this.getClass().getName(), "Error in decodeTagType4A: CC file length is null");
						retRes = SUCCESS_RES;
					} else {
						// - Read CC content
						byte[] CCBinary = new byte[CCLength];
						retRes = _STTagHandler.requestCCRead(CCLength, CCBinary);
						if (retRes != SUCCESS_RES) {
							Log.v(this.getClass().getName(),
									"Error in decodeTagType4A: Failed to read CC file content");
							retRes = SUCCESS_RES;
						} else {
							_CCHandler = new stnfccchandler(CCBinary);
						}
					}
				}
			} catch (Exception e) {
				// If an error occurred in any of these operations, log it, and
				// remove the data
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: Exception when accessing CC file");
				_CCHandler = null;
			}

			try {
				// Get NDEF File
				// This is possible only if we could get the CC file...
				if (_CCHandler == null) {
					Log.v(this.getClass().getName(),
							"Error in decodeTagType4A: Cannot read NDEF file (missing CC File)");
				} else {
					// - Select NDEF File
					if ((((stnfccchandler) (_CCHandler)).getnbNDEFFile() == 0)
							&& (TLVBlockID > ((stnfccchandler) (_CCHandler)).getnbNDEFFile())) {
						Log.v(this.getClass().getName(), "Error in decodeTagType4A: can't select request File ID");
						retRes = SUCCESS_RES;
					}
					// retRes =
					// _STTagHandler.selectNdef(_CCHandler.getfieldId(TLVBlockID));
					GenErrorAppReport appret = _STTagHandler
							.selectNdef(((stnfccchandler) (_CCHandler)).getfieldId(TLVBlockID));
					retRes = appret.m_err_value;

					if (retRes != SUCCESS_RES) {
						Log.v(this.getClass().getName(), "Error in decodeTagType4A: Cannot select NDEF file");
						retRes = SUCCESS_RES;
					}
				}
			} catch (Exception e) {
				// If an error occurred in any of these operations, log it, and
				// remove the data
				Log.v(this.getClass().getName(), "Error in decodeTagType4A: Exception when accessing NDEF file");
				_NDEFHandlerArray[TLVBlockID] = null;
				_NDEFSimplifiedHandlerArray[TLVBlockID] = null;
			}

			// Close current connection
			// _STTagHandler.closeConnection();
		}
		return retRes;
	}

	public boolean pingTag() {
		NdefFormatable lNdefFormatable = null;
		// NFCTag currentTag = ((NFCApplication)
		// getApplication()).getCurrentTag();
		NFCTag currentTag = this;
		Ndef lNdefTag = Ndef.get(currentTag.getTag());

		if (currentTag.getType() == NfcTagTypes.NFC_TAG_TYPE_4A) {
			IsoDep lIsoDepTag = IsoDep.get(currentTag.getTag());
			if (lIsoDepTag != null) {
				try {
					// FBE .... issue when IllegalStateException
					lIsoDepTag.close();
					lIsoDepTag.connect();
					if (lIsoDepTag.isConnected()) {
						lIsoDepTag.close();
						return true;
					} else {
						lIsoDepTag.close();
						return false;
					}

				} catch (IOException e) {
					Log.d(TAG, "IOException while Tag Ping !");
					return false;

				}
			}
		}

		if (currentTag.getType() == NfcTagTypes.NFC_TAG_TYPE_V) {
			NfcV nfcvTag = NfcV.get(currentTag.getTag());
			if (nfcvTag != null) {
				try {
					nfcvTag.close();
					nfcvTag.connect();
					if (nfcvTag.isConnected()) {
						nfcvTag.close();
						return true;
					} else {
						nfcvTag.close();
						return false;
					}

				} catch (IOException e) {
					Log.d(TAG, "IOException while Tag Ping !");
					return false;

				}
			}
		}

		if (lNdefTag == null) {
			lNdefFormatable = NdefFormatable.get(currentTag.getTag());
			if (lNdefFormatable != null) {
				try {
					lNdefFormatable.close(); // FBE
					lNdefFormatable.connect();
					if (lNdefFormatable.isConnected()) {
						lNdefFormatable.close();
						return true;
					} else {
						lNdefFormatable.close();
						return false;
					}

				} catch (IOException e) {
					Log.d(TAG, "IOException while Tag Ping !");
					return false;

				}
			} else
				return false;
		} else {
			try {
				lNdefTag.close(); // FBE
				lNdefTag.connect();
				if (lNdefTag.isConnected()) {
					lNdefTag.close();
					return true;
				} else {
					lNdefTag.close();
					return false;
				}

			} catch (IOException e) {
				// cannot "connect to a tag .. go through WaitForTAPActivity
				Log.d(TAG, "IOException while Tag Ping !");
				return false;
			}
		}

	}

	private Toast WFNFCTAPToast;

	private void toastStatus(String status) {

		// Create Toast to inform user on the Tool request process.
		// Context context = getApplicationContext();
		Context context = NFCApplication.getContext();// getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		WFNFCTAPToast = Toast.makeText(context, status, duration);
		WFNFCTAPToast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		WFNFCTAPToast.show();

	}

	public int reportactionstatus(String status, int i) {
		Log.d("TAG", status);
		toastStatus(status);
		return i;
	}

}
