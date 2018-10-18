package com.teamwizardry.wizardry.lib.vm.utils.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ParserUtils {
	private ParserUtils() {}
	
	public static String readFromStream(InputStream sin) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(sin));
		
		try {
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if( builder.length() > 0 )
					builder.append('\n');
				builder.append(line);				
			}
			return builder.toString();
		}
		finally {
			try {
				br.close();
			}
			catch(IOException exc) {
				// TODO: Log it!
				exc.printStackTrace();
			}
		}
	}
	
	public static String readFromResource(String resName) throws IOException {
		InputStream stream = ParserUtils.class.getResourceAsStream(resName);
		if( stream == null )
			throw new IOException("Couldn't open resource '" +resName + "'");
		try {
			return readFromStream(stream);
		}
		finally {
			try {
				stream.close();
			}
			catch(IOException exc) {
				// TODO: Log it!
				exc.printStackTrace();
			}
		}
	}
	
	public static boolean isSeparator(char c) {
		if( c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f' )
			return true;
		return false;
	}
	
	public static boolean isEOL(char c) {
		if( c == '\r' || c == '\n' )
			return true;
		return false;
	}
	
	public static boolean isKeywordChar(char c, boolean bIsFirst) {
		if( Character.isAlphabetic(c) )
			return true;
		if( !bIsFirst && Character.isDigit(c) )
			return true;
		if( c == '_' )
			return true;	// Exceptional character
		return false;
	}
	
	public static String keywordify(String str) {
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < str.length(); i ++ ) {
			char c = str.charAt(i);
			if( !isKeywordChar(c, i == 0) ) {
				builder.append('C');
				builder.append(Integer.toString(c));
			}
			else
				builder.append(c);
		}
		return builder.toString();
	}
}
