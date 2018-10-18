package com.teamwizardry.wizardry.lib.vm.utils.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class StringPreProcessor {
	private final Node root = new Node();
	private final String input;
	
	public StringPreProcessor(String input) {
		this.input = input;
	}
	
	public StringPreProcessor(InputStream sin) throws IOException {
		this.input = ParserUtils.readFromStream(sin);
	}
	
	public StringPreProcessor setReplacement(String key, String replaceWith) {
		if( key.isEmpty() )
			throw new IllegalArgumentException("Key is empty.");
		
		Node cur = root;
		for( int i = 0; i < key.length(); i ++ ) {
			char c = key.charAt(i);
			if( !ParserUtils.isKeywordChar(c, i == 0) )
				throw new IllegalArgumentException("Key contains invalid character '" + c + ((i==0)?"' on the beginning.":"'."));
			cur = cur.getChild(c, true);
		}
		cur.setValue(replaceWith);
		return this;
	}
	
	public String build() {
		StringBuilder builder = new StringBuilder(input.length());
		
		int scanMode = 0; // 0 = try replace, 1 = ignore, 2 = replace on # pre, 3 = try replace on ##, 4 = replace on # pre (hidden), 5 = try replace on ## (hidden)
		Node cur = root;
		for( int i = 0; i < input.length(); i ++ ) {
			char c = input.charAt(i);
			
			if( scanMode == 1 ) {
				if( c == '#' )
					scanMode = 2;
				else {
					if( !ParserUtils.isKeywordChar(c, true) &&
						!ParserUtils.isKeywordChar(c, false) )
						scanMode = 0;
					builder.append(c);
				}
			}
			else if( scanMode == 2 || scanMode == 4 ) {
				if( c == '#' ) {
					if( scanMode == 2 )
						scanMode = 3;
					else
						scanMode = 5;
				}
				else {
					builder.append('#');
					builder.append(c);
					scanMode = 1;
				}
			}
			else if( scanMode == 0 || scanMode == 3 || scanMode == 5 ) {
				if( c == '#' || !ParserUtils.isKeywordChar(c, cur.getDepth() == 0) ) {
					// Finalize replacement
					String value = cur.getValue();
					if( value != null )
						builder.append(value);
					else {
						if( scanMode == 3 )
							builder.append("##");
						builder.append(cur.getSubKey());
					}
					cur = root;
					
					if( c == '#' ) {
						if( value == null )
							scanMode = 2;
						else
							scanMode = 4;
					}
					else {
						builder.append(c);
						scanMode = 0;
					}
				}
				else {
					// Collect
					Node nextNode = cur.getChild(c, false);
					if( nextNode == null ) {
						// Abort collecting
						if( scanMode == 3 )	// Also react on 5
							builder.append("##");
						builder.append(cur.getSubKey());
						builder.append(c);
						cur = root;
						scanMode = 1;
					}
					else
						cur = nextNode;
				}
			}
			else
				throw new IllegalStateException("Invalid scan mode " + scanMode);
		}
		
		// Abort running states
		if( scanMode == 1 ) {
			// Do nothing.
		}
		else if( scanMode == 2 || scanMode == 4 )
			builder.append('#');
		else if( scanMode == 0 || scanMode == 3 || scanMode == 5 ) {
			if( scanMode == 3 )
				builder.append("##");
			builder.append(cur.getSubKey());
		}
		
		return builder.toString();
	}
	
	private static class Node {
		private final char key;
		private final int depth;
		private final Node parent;
		private final HashMap<Character, Node> children = new HashMap<>();
		private String value = null;
		
		Node() {
			this.key = '\0';
			this.parent = null;
			this.depth = 0;
		}
		
		private Node(Node parent, char c) {
			this.key = c;
			this.parent = parent;
			this.depth = parent.depth + 1;
		}
		
		void setValue(String value) {
			this.value = value;
		}
		
		String getValue() {
			return value;
		}
		
		int getDepth() {
			return depth;
		}
		
		String getSubKey() {
			StringBuilder builder = new StringBuilder();
			Node cur = this;
			while( cur.parent != null ) {
				builder.append(cur.key);
				cur = cur.parent;
			}
			return builder.reverse().toString();
		}
		
		Node getChild(char c, boolean createIfNotExisting) {
			Node child = children.get(Character.valueOf(c));
			if( child == null ) {
				if( !createIfNotExisting )
					return null;
				child = new Node(this, c);
				children.put(c, child);
			}
			return child;
		}
	}
}
