package com.objectmentor.utilities.getopts;

import java.util.*;

public class Args { 　
    private String schema; 　
    private String[] args; 　
    private boolean valid; 　
    private Set<Character> unexpectedArguments = new TreeSet<Character>(); 　
    private Map<Character, Boolean> booleanArgs = new HashMap<Character, Boolean>(); 　
    private int numberOfArguments = 0; 　

    public Args(String schema, String[] args) { 　 　
        this.schema = schema; 　 　
        this.args = args; 　 　
        valid = parse(); 　
    } 　

    public boolean isValid() { 　 　
        return valid; 　
    } 　

    private boolean parse() { 　 　
        if (schema.length() == 0 && args.length == 0) 　 　 　
            return true; 　 　
        parseSchema(); 　 　
        parseArguments(); 　 　
        return unexpectedArguments.size() == 0; 　
    } 　
    
    /**Begin to parse Schema e.g. "l" 
     * e.g. schema = "l"
     * e.g. booleanArgs = 'l':false
     * **/
    private boolean parseSchema() { 　 　
        for (String element : schema.split(",")) { 　 　 　
            parseSchemaElement(element); 　 　
        } 　 　
        return true; 　
    } 　

    private void parseSchemaElement(String element) { 　 　
        if (element.length() == 1) { 　 　 　
            parseBooleanSchemaElement(element); 　 　
        } 　
    } 　
    
    //intiate booleanArgs with SchemaElement and default Argument false
    private void parseBooleanSchemaElement(String element) { 　 　
        char c = element.charAt(0); 
        if (Character.isLetter(c)) { 　 　 　
            booleanArgs.put(c, false); 　 　
        } 　
    } 　
    /** Begin to parse Argument e.g. "-l" 
     *  e.g. args = ["-l", "true"], booleanArgs = <'l':true>
     *       args = ["-ls"], booleanArgs = <'l':true>, unexpectedArguments = {'s'}
     *       args = ["-l"], booleanArgs = <'l':true>
     *       args = ["-p", "true"], unexpectedArguments = {'p'}
     *  
     * **/

    private boolean parseArguments() { 　 　
        for (String arg : args) 　 　 　
            parseArgument(arg); 　 　
        return true; 　
    } 　

    //startwith "-" means it is followed by a schemaElement
    private void parseArgument(String arg) { 　 　
        if (arg.startsWith("-")) 　 　 　
            parseElements(arg); 　
    } 　

    private void parseElements(String arg) { 　 　
        for (int i = 1; i < arg.length(); i++) 　 　 　
            parseElement(arg.charAt(i)); 　
    } 　
    
    private void parseElement(char argChar) { 　 　
        if (isBoolean(argChar)) { 　 　 　
            numberOfArguments++; 　 　 　
            setBooleanArg(argChar, true); 　 　
        } 
        else 　 　 　
            unexpectedArguments.add(argChar); 　//"-L"
    } 　

    private void setBooleanArg(char argChar, boolean value) { 　 　
        booleanArgs.put(argChar, value); 　
    } 　
    
    //If Schema Hashmap contains the key 
    private boolean isBoolean(char argChar) { 　 　
        return booleanArgs.containsKey(argChar); 　
    } 　

    public int cardinality() { 　 　
        return numberOfArguments; 　
    }

    public String usage() { 　 　
        if (schema.length() > 0) 　 　 　
            return "-[" + schema + "]"; 　 　 
        else 　 　 　
            return ""; 　} 　

    public String errorMessage() { 　 　
        if (unexpectedArguments.size() > 0) { 　 　 　
            return unexpectedArgumentMessage(); 　 　
        } 
        else 　 　 　
            return ""; 　
    } 　

    private String unexpectedArgumentMessage() { 　 　
        StringBuffer message = new StringBuffer(" Argument( s) 　-"); 　 　
        for (char c : unexpectedArguments) { 　 　 　
            message.append(c); 　 　
        } 　 　
        message.append(" 　 unexpected."); 　 　
        return message.toString(); 　
    }

