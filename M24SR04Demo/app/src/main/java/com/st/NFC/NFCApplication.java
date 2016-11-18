/*
  * Author					:  MMY Application Team
  * Last committed			:  $Revision: 1504 $
  * Revision of last commit	:  $Rev: 1504 $
  * Date of last commit     :  $Date: 2016-01-04 13:59:27 +0100 (Mon, 04 Jan 2016) $ 
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Application;
import android.content.Context;

import android.os.Environment;
import com.st.NFC.NFCTag;

public class NFCApplication extends Application {
	private static NFCApplication currentApp = null;
	// Implements NFC application specific global variable 
	private NFCTag currentTag;
	private String uid;
	private int fileID; // Required to store current 
						// ID file in case of several tapping.
	private boolean enableDemoFeature = false;
	private boolean enableSalonFeature = false;
	
	
	private String mNFCApp_customerSociete;
	public String getmNFCApp_customerSociete() {
		if (mNFCApp_customerSociete == null) {
			return new String("NotProvided");
		} else
		return mNFCApp_customerSociete;
	}

	public void setmNFCApp_customerSociete(String mNFCApp_customerSociete) {
		this.mNFCApp_customerSociete = mNFCApp_customerSociete;
	}

	// additional info used for Show/salon and message mail construction
	private String mNFCApp_customername;
	public String getmNFCApp_customername() {
		if (mNFCApp_customername == null) {
			return new String("Name !: No information initialized - Please Tap a business VCard before according to use case context");
		} else
		return mNFCApp_customername;
	}

	public void setmNFCApp_customername(String mNFCApp_customername) {
		this.mNFCApp_customername = mNFCApp_customername;
	}

	private String mNFCApp_customermail;
	public String getmNFCApp_customermail() {
		if (mNFCApp_customermail == null) {
			return new String("No information initialized - VCard info needed");
		} else
		return mNFCApp_customermail;

	}

	public void setmNFCApp_customermail(String mNFCApp_customermail) {
		this.mNFCApp_customermail = mNFCApp_customermail;
	}

	private String mNFCApp_customertextinformation;
	
	public String getmNFCApp_customertextinformation() {
		if (mNFCApp_customertextinformation == null) {
			return new String("Text part !: No information initialized - URI,Text,...needed");
		
		} else
		return mNFCApp_customertextinformation;
	}

	public void setmNFCApp_customertextinformation(String mNFCApp_customertextinformation) {
		this.mNFCApp_customertextinformation = mNFCApp_customertextinformation;
	}

	/**
	 * @return the enableDemoFeature
	 */
	public boolean isEnableDemoFeature() {
		return enableDemoFeature;
	}
	public boolean isEnableSalonFeature() {
		return enableSalonFeature;
	}

	/**
	 * @param enableDemoFeature the enableDemoFeature to set
	 */
	public void setEnableDemoFeature(boolean enableDemoFeature) {
		this.enableDemoFeature = enableDemoFeature;
	}
	public void setEnableSalonFeature(boolean enableFeature) {
		this.enableSalonFeature = enableFeature;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		fileID = -1; // default value. No Tag has been parsed yet
		currentApp = this;
	}

	public static NFCApplication getApplication() {
		return currentApp;
	}

	public static Context getContext() {
		return currentApp.getApplicationContext();
	}

	public void setCurrentTag(NFCTag currentTag) {
		this.currentTag = currentTag;
	}

	public NFCTag getCurrentTag() {
		return currentTag;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}
	
	public int getFileID()
	{
		return fileID;
	}
	
	public void setFileID( int ID)
	{
		fileID = ID;
	}
	
	
	// File management
	
	
	public String getMailSalonHeader(){
		return getExternalStorageMail("mailheader.txt");
	}
	public String getMailSalonFooter(){
		return getExternalStorageMail("mailfooter.txt");
	}

	public String getMailSalonSubject(){
		return getExternalStorageMail("mailsubject.txt");
	}

	private String getExternalStorageMail(String filename) {
		//File str = Environment.getExternalStorageDirectory();
		File str = this.getBaseContext().getExternalFilesDir(null);
		String ret = null;
		// Get the text file
		File file = new File(str, filename);
		try {
			ret = getStringFromFile(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	private  String getStringFromFile (File filePath) {
		//Read text from file
		StringBuilder text = new StringBuilder();

		try {
		    BufferedReader br = new BufferedReader(new FileReader(filePath));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();
		}
		catch (IOException e) {
		    //You'll need to add proper error handling here
			text.append(filePath.getName() + "!: No content found. Path: " + filePath.getPath());
		}
		return text.toString();
	}	
}
