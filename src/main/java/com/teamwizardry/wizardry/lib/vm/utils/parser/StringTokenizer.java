package com.teamwizardry.wizardry.lib.vm.utils.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class StringTokenizer {
	private final String input;
	private Token nextToken;
	private int curPos;
	
//	private boolean hasModeKeywords = true;
	
	public StringTokenizer(InputStream sin) throws IOException, ScriptParserException {
		this.input = ParserUtils.readFromStream(sin);
		this.curPos = 0;
		
		this.nextToken = scanNextToken();
	}
	
	public StringTokenizer(String input) throws ScriptParserException {
		this.input = input;
		this.curPos = 0;
		
		this.nextToken = scanNextToken();
	}
	
/*	public StringTokenizer setMode(TokenizerMode mode, boolean enabled) {
		if( TokenizerMode.HAS_KEYWORDS.equals(mode) ) {
			hasModeKeywords = enabled;
		}
		else
			throw new IllegalArgumentException("Mode '" + mode + "' not supported.");
		return this;
	} */
	
	public boolean hasNextToken() {
		return nextToken != Token.NULL;
	}
	
	public Token getNextToken() throws ScriptParserException {
		Token retNextToken = nextToken;
		nextToken = scanNextToken();
		return retNextToken;
	}
	
	private Token scanNextToken() throws ScriptParserException {
		ProcessMode procMode = ProcessMode.DEFAULT;
		int tokenStart = -1;
		int keywordEndPos = -1;	// Only for KEYWORD1 and KEYWORD2
		int textEndPos = -1;
		
		curPos = skipSeparators(curPos);
		
		int i;
		for( i = curPos; i < input.length(); i ++ ) {
			char c = input.charAt(i);
			
			if( procMode.equals(ProcessMode.DEFAULT) ) {
				// Common
				if( Character.isDigit(c) ) {
					tokenStart = i;
					procMode = ProcessMode.INTEGER;
				}
				else if( ParserUtils.isKeywordChar( c, true ) ) {
					tokenStart = i;
					procMode = ProcessMode.KEYWORD1;
				}
				else if( c == '/' ) {
					procMode = ProcessMode.COMMENT_WHAT;
				}
				else if( c == '"' ) {
					procMode = ProcessMode.TEXT;
					tokenStart = i + 1;
				}
				else if( ParserUtils.isSeparator( c ) ) {
					if( tokenStart == -1 )
						throw new IllegalStateException("Should not happen");
					break;
				}
				else {
					tokenStart = i;
					i++;
					break;
				}
			}
			else if( procMode.equals(ProcessMode.TEXT) ) {
				// TODO: Character escape!
				if( c == '"' ) {
					textEndPos = i;
					i ++;
					break;
				}
			}
			else if( procMode.equals(ProcessMode.KEYWORD1) ) {
				// Keywords
				if( !ParserUtils.isKeywordChar( c, false ) ) {
					keywordEndPos = i;
					
					if( ParserUtils.isSeparator( c ) ) {
						i = skipSeparators( i );
						c = input.charAt(i);
					}
					
					if( c == '.' ) {
						i = skipSeparators( i + 1 ) - 1;
						procMode = ProcessMode.KEYWORD2;
						continue;
					}
					else
						break;
				}
			}
			else if( procMode.equals(ProcessMode.KEYWORD2) ) {
				// Process domain
				if( ParserUtils.isKeywordChar( c, true ) ) {
					procMode = ProcessMode.KEYWORD1;
				}
				else {
					// Rollback to keyword end pos
					i = keywordEndPos;
					break;
				}
			}
			else if( procMode.equals(ProcessMode.COMMENT_WHAT) ) {
				// Maybe a comment
				if( c == '*' ) {
					procMode = ProcessMode.COMMENT_BRACKET1;
				}
				else if( c == '/' )
					procMode = ProcessMode.COMMENT_LINE;
				else {
					// Otherwise print the last token
					tokenStart = i - 1;
					break;
				}
			}
			else if( procMode.equals(ProcessMode.COMMENT_LINE) ) {
				// Comment line
				if( ParserUtils.isEOL( c ) ) {
					i = skipSeparators( i + 1 ) - 1;	// Separators are always skipped when entering procMode = 0
					procMode = ProcessMode.DEFAULT;
					continue;
				}
			}
			else if( procMode.equals(ProcessMode.COMMENT_BRACKET1) ) {
				// Comment brackets
				if( c == '*' )
					procMode = ProcessMode.COMMENT_BRACKET2;
			}
			else if( procMode.equals(ProcessMode.COMMENT_BRACKET2) ) {
				if( c == '/' ) {
					i = skipSeparators( i + 1 ) - 1;	// Separators are always skipped when entering procMode = 0
					procMode = ProcessMode.DEFAULT;
					continue;
				}
				else {
					procMode = ProcessMode.COMMENT_BRACKET1;
				}
			}
			else if( procMode.equals(ProcessMode.INTEGER) ) {
				// Keywords
				if( !Character.isDigit( c ) ) {
					break;
				}
			}
		}
		curPos = i;
		
		if( tokenStart >= 0 ) {
			TokenType type;
			if( procMode.equals(ProcessMode.DEFAULT) )
				type = TokenType.SIGN;
			else if( procMode.equals(ProcessMode.KEYWORD1) ||
					 procMode.equals(ProcessMode.KEYWORD2) )
				type = TokenType.KEYWORD;
			else if( procMode.equals(ProcessMode.COMMENT_WHAT) ||
					 procMode.equals(ProcessMode.COMMENT_LINE) ||
					 procMode.equals(ProcessMode.COMMENT_BRACKET1) ||
					 procMode.equals(ProcessMode.COMMENT_BRACKET2) )
				throw new IllegalStateException("Shouldn't happen. A comment has no token representation.");
			else if( procMode.equals(ProcessMode.INTEGER) )
				type = TokenType.INTEGER;
			else if( procMode.equals(ProcessMode.TEXT) ) {
				if( textEndPos < 0 )
					throw new ScriptParserException("Text bracket not closed.");
				type = TokenType.TEXT;
			}
			else
				throw new IllegalStateException("Illegal procMode.");
			
			String s;
			if( !procMode.equals(ProcessMode.TEXT) )
				s = input.substring(tokenStart, curPos);
			else
				s = input.substring(tokenStart, textEndPos);
			if( type.equals(TokenType.KEYWORD) )
				return new Token(extractDomain(s), type);
			else
				return new Token(new String[] { s }, type);
		}
		return Token.NULL;
	}
	
	///////
	
	private int skipSeparators( int curPos ) {
		int i;
		for( i = curPos; i < input.length(); i ++ ) {
			char c = input.charAt(i);
			if( !ParserUtils.isSeparator( c ) )
				break;
		}
		return i;
	}
	
	private String[] extractDomain(String s) {
		LinkedList<String> domainList = new LinkedList<>();
		
		int beginStr = -1;
		for( int i = 0; i < s.length(); i ++ ) {
			char c = s.charAt(i);
			if( c == '.' || ParserUtils.isSeparator(c) ) {
				if( beginStr != -1 ) {
					domainList.add(s.substring(beginStr, i));
					beginStr = -1;
				}
			}
			else {
				if( beginStr == -1 ) 
					beginStr = i;
			}
		}
		if( beginStr != -1 )
			domainList.add(s.substring(beginStr, s.length()));

		return domainList.toArray(new String[domainList.size()]);
	}
	
	////
	
	private static enum ProcessMode {
		DEFAULT,
		KEYWORD1,
		KEYWORD2,
		COMMENT_WHAT,
		COMMENT_LINE,
		COMMENT_BRACKET1,
		COMMENT_BRACKET2,
		INTEGER,
		TEXT
	}
	
/*	public static enum TokenizerMode {
		HAS_KEYWORDS
	} */
	
	public static enum TokenType {
		NULL,
		KEYWORD,
		SIGN,
		INTEGER,
		TEXT
	}
	
	public static class Token {
		private final String[] token;
		private final TokenType type;
		
		private final String reprString;
		private final int reprInteger;
		
		public static final Token NULL = new Token(null, TokenType.NULL);
		
		Token(String[] token, TokenType type) {
			this.token = token;
			this.type = type;
			this.reprString = createRepresentationString();
			this.reprInteger = createRepresentationInteger();
		}
		
		public TokenType getType() {
			return this.type;
		}
		
		public boolean isType(TokenType type) {
			return this.type.equals(type);
		}
		
		public String getDomainNode(int i) {
			if( !isType( TokenType.KEYWORD ) )
				return null;
			if( i < 0 || i >= token.length )
				return null;
			return token[i];
		}
		
		public int getCountDomainNodes() {
			if( !isType( TokenType.KEYWORD ) )
				return 0;
			return token.length;
		}
		
		public String getSubDomain(int iFirst, int iLast) {
			if( !isType( TokenType.KEYWORD ) )
				return null;
			
			StringBuilder builder = new StringBuilder();
			for( int j = iFirst; j < iLast; j ++) {
				if( j > iFirst )
					builder.append('.');
				builder.append(token[j]);
			}
			return builder.toString();
		}

		public boolean isKeyword(String keyw) {
			if( !isType( TokenType.KEYWORD ) )
				return false;
			return this.reprString.equals(keyw);
		}

		public boolean isSign(char c) {
			if( !isType(TokenType.SIGN) )
				return false;
			return this.reprString.equals("" + c);
		}
		
		public boolean isInteger(int i) {
			if( !isType(TokenType.INTEGER) )	// TODO: Support float as well! 
				return false;
//			if( reprInteger == null )
//				return false;
			return reprInteger == i;
		}
		
		private static String concatTypes(Object[] types) {
			// TODO: Move to Utils!
			if( types.length <= 0 )
				return "";
			if( types.length == 1 )
				return types.toString();
			
			StringBuilder builder = new StringBuilder();
			builder.append('[');
			for( Object obj : types ) {
				if( builder.length() > 0 )
					builder.append(", ");
				builder.append(obj.toString());
			}
			builder.append(']');
			return builder.toString();
		}
		
		public Token expectCompatibleTypes(TokenType ... types) throws ScriptParserException {
			for( TokenType t : types ) {
				if( isType(t) )
					return this;
			}
			throw new ScriptParserException("Expected a token of at least type " + concatTypes(types) + " but got '" + reprString + "'.");
		}
		
		public Token expectKeyword(String keyw) throws ScriptParserException {
			if( !isKeyword(keyw) )
				throw new ScriptParserException("Expected keyword '" + keyw + "' but got '" + reprString + "'.");
			return this;
		}
		
		public Token expectSign(char c) throws ScriptParserException {
			if( !isSign(c) )
				throw new ScriptParserException("Expected sign '" + c + "' but got '" + reprString + "'.");
			return this;
		}
		
		public Token expectInteger(int i) throws ScriptParserException {
			if( !isInteger(i) )
				throw new ScriptParserException("Expected integer " + i + " but got '" + reprString + "'.");
			return this;
		}
		
		public Token exceptText(String text) throws ScriptParserException {
			if( !isType(TokenType.TEXT) ||
				!reprString.equals(text) ) {
				throw new ScriptParserException("Expected a text string '" + text + "' but got '" + reprString + "'" );
			}
			return this;
		}
		
		public String toString() {
			return this.reprString;
		}
		
		public int toInteger() throws ScriptParserException {
			if( !isType(TokenType.INTEGER) )
				throw new ScriptParserException("Incompatible type. '" + reprString + "' is not an integer.");
//			if( reprInteger == null )
//				throw new ScriptParserException("Couldn't convert '" + reprString + "' to an integer.");
			return reprInteger;
		}
		
		public Object toValue() {
			if( isType(TokenType.INTEGER) ) {
				return Integer.valueOf(reprInteger);
			}
			else {
				return reprString;
			}
		}
		
		/////
		
		private String createRepresentationString() {
			if( type.equals(TokenType.NULL) )
				return null;
			else if( type.equals(TokenType.KEYWORD) ) {
				if( token.length == 1 )
					return token[0];
				else {
					StringBuilder builder = new StringBuilder();
					for( int i = 0; i < token.length; i ++ ) {
						if( i >= 1 )
							builder.append('.');
						builder.append(token[i]);
					}
					return builder.toString();
				}
			}
			else if( type.equals(TokenType.SIGN) || type.equals(TokenType.INTEGER) || type.equals(TokenType.TEXT) )
				return token[0];
			else
				throw new IllegalStateException("Unknown token type.");
		}
		
		private int createRepresentationInteger() {
			if( type != TokenType.INTEGER )
				return 0;
			try {
				return Integer.parseInt(reprString);
			}
			catch(NumberFormatException exc) {
				throw new IllegalStateException("String '" + reprString + "' is not parseable as integer.", exc);
			}
		}
	}
}
