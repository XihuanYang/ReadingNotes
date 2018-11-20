# Version 1
1. booleanArgs是存储schema和args的地方
    unexpectedArguments记录输入的args数组中是否存在不符合schema的字符
    numberOfArguments是记录从args解析出来arg的数目
2. bool类型的参数处理，有个特点就是即使schema "-l" 之后不需要带arg， 默认没有"-l"就是false, 有"-l"就是true
3. 对于一些一场输入能够处理
```
args = ["-l", "true"], booleanArgs = <'l':true>
args = ["-ls"], booleanArgs = <'l':true>, unexpectedArguments = {'s'}
args = ["-l"], booleanArgs = <'l':true>
args = ["-p", "true"], unexpectedArguments = {'p'}
```
4. version 1仅支持bool类型的参数处理
5. 错误是通过定义一个bool类型valid， 如果unexpectedArguments数量不为0，那么可以认为parse()存在问题返回的是false, 外部需要主动调用类方法isValid()来获取parse的正确与否，同时如果parse错误，那么又需要通过调用类方法errorMessage() 来获取对应的错误信息， 错误信息支持处理打印出unexpectedArguments
```java
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
```

# Version 2
1. 增加了stringArgs存储string类型的schema和args的HashMap
   增加了argsFound用于存储argument， 并删除了numberOfArguments, 这样不仅可以获取argument内容也可以获取个数
2. 增加了`throws ParseException`来实时抛出Parse异常
https://docs.oracle.com/javase/7/docs/api/java/text/ParseException.html
3. parseSchema()增加了对element长度的判断，用于避免对0长度的element进行trim()
e.g. schema = "l, d*,"
```java
    private boolean parseSchema() throws ParseException { 　 　
        for (String element : schema.split(",")) { 　 　 　
            if (element.length() > 0) { 　 　 　 　
                String trimmedElement = element.trim(); 　 　 　 　
                parseSchemaElement(trimmedElement); 　 　 　
            } 　 　
        } 　 　
        return true; 　
    } 
```
vs
```java
    private boolean parseSchema() { 　 　
        for (String element : schema.split(",")) { 　 　 　
            parseSchemaElement(element); 　 　
        }
        return true; 　
    }
```
4. 使用函数validateSchemaElementId()SchemaElementId进行校验，如果校验失败，抛出具有错误信息的ParseException，以前版本如果出现非valid的SchemaElementId就忽略不处理，不抛错
5. 增加了对string类型的schema的支持， 从这里看出来每增加一个类型，就需要修改parseSchemaElement， 增加parseTypeElement()和isTypeSchemaElement()函数
```java
    private void parseSchemaElement(String element) throws ParseException { 　 　
        char elementId = element.charAt(0); 　 　
        String elementTail = element.substring(1); 　 　
        validateSchemaElementId(elementId); 　 　
        if (isBooleanSchemaElement(elementTail)) 　 　 　
            parseBooleanSchemaElement(elementId); 　 　 
        else if (isStringSchemaElement(elementTail)) 　 　 　
            parseStringSchemaElement(elementId); 　
    } 　

    private void validateSchemaElementId(char elementId) throws ParseException { 　 　
        if (!Character.isLetter(elementId)) { 　 　 　
            throw new ParseException(
                "Bad character:" + elementId + "in 　 Args 　 format: 　" + schema, 0); 　 　
        } 　
    } 　

    private void parseStringSchemaElement(char elementId) { 　 　
        stringArgs.put(elementId, ""); 　
    } 　

    private boolean isStringSchemaElement(String elementTail) { 　 　
        return elementTail.equals("*"); 　
    } 　

    private boolean isBooleanSchemaElement(String elementTail) { 　 　
        return elementTail.length() == 0; 　
    } 　

    private void parseBooleanSchemaElement(char elementId) { 　 　
        booleanArgs.put(elementId, false); 　
    }
```
vs
```java
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
```
6. 增加了对String Argument的支持， 将获取到的Argument设置到对应的HashMap单独抽取到函数setArgument， 如果需要增加对其他类型的支持，需要修改setArgument()， 增加isType()和setTypeArg()
7. 删除了通过判断unexpectedArguments的size与0比对的逻辑来判断是否存在不合理的argument， 直接通过发现invalid argument就赋值valid为false
8. 下面代码本身还存在小瑕疵， return false，非得映入一个多余变量， setStringArg还非得加一个用不到的参数s
```java
    private void parseElement(char argChar) { 　 　
        if (setArgument(argChar)) 　 　 　
            argsFound.add(argChar); 　 　 
        else{ 　 　 　
            unexpectedArguments.add(argChar); 　 　 　
            valid = false; 　 　
        } 　
    }
    private boolean setArgument(char argChar) { 　 　
        boolean set = true; 　 　
        if (isBoolean(argChar)) 　 　 　
            setBooleanArg(argChar, true); 　 　 
        else if (isString(argChar)) 　 　 　
            setStringArg(argChar, ""); 　 　 
        else 　 　 　
            set = false; 　 　
            return set; 　
    }
    private void setStringArg(char argChar, String s) { 　 　
        currentArgument++; 　 　
        try { 　 　 　
            stringArgs.put(argChar, args[currentArgument]); 　 　
        } 
        catch (ArrayIndexOutOfBoundsException e) { 　 　 　
            valid = false; 　 　 　
            errorArgument = argChar; 　 　 　
            errorCode = ErrorCode.MISSING_ STRING; 　 　
        } 　
    } 　

    private boolean isString(char argChar) { 　 　
        return stringArgs.containsKey(argChar); 　
    } 　

    private void setBooleanArg(char argChar, boolean value) { 　 　
        booleanArgs.put(argChar, value); 　
    } 　

    private boolean isBoolean(char argChar) { 　 　
        return booleanArgs.containsKey(argChar); 　
    } 　
```
vs
```java
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
```
9. error的处理不仅仅是针对argument中出现非预期的schema, 同时也增加了对于String这种类型的Argument不可缺失的错误信息，但是对于OK这种设定真是费解， 总体来说如果有新的Type需要支持errorMessage()需要随之改动
```java
    public String errorMessage() throws Exception { 　 　
        if (unexpectedArguments.size() > 0) { 　 　 　
            return unexpectedArgumentMessage(); 　 　
        } 
        else 　 　 　
        switch (errorCode) { 　 　 　 　
            case MISSING_STRING: 　 　 　 　 　
                return String.format(" Could 　 not 　 find 　 string 　 parameter 　 for 　-% c.", errorArgument); 　 　 　 　
            case OK: 　 　 　 　 　
                throw new Exception(" TILT: 　 Should 　 not 　 get 　 here."); 　 　 　
        } 　 　
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
```
vs
```java
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
```
10. 增加了对对应schema的argument的获取， 和argument是否存在的判断
```java
   public boolean getBoolean(char arg) { 　 　
        return falseIfNull(booleanArgs.get(arg)); 　
    } 　

    private boolean falseIfNull(Boolean b) { 　 　
        return b == null ? false : b; 　
    } 　

    public String getString(char arg) { 　 　
        return blankIfNull(stringArgs.get(arg)); 　
    } 　

    private String blankIfNull(String s) { 　 　
        return s == null ? "" : s; 　
    } 　

    public boolean has(char arg) { 　 　
        return argsFound.contains(arg); 　
    }
```
# Version 3
1. 增加了对Integer类型参数的解析
2. 增加了intArgs用于存储Interger类型的schema和argument的HashMap
3. 增加了errorArgumentId和errorParameter
4. ErrorCode枚举整合了UNEXPECTED_ ARGUMENT， 并且新增了INTEGER类型的错误
```java
    private enum ErrorCode {　 　 
           OK, MISSING_ STRING, 
           MISSING_ INTEGER, INVALID_ INTEGER, 
           UNEXPECTED_ ARGUMENT 　 
    }
```
发生错误的时候首先初始化一下errorCode，此前只有MISSING_ STRING和一个费解的OK， 现在针对parseArguments()的过程中出现的
* 非预期的schema类型 e.g. -s "slkd", 设置errorCode， Version 2中不处理errorCode
```java
    private void parseElement(char argChar) throws ArgsException {　 　 
        if (setArgument(argChar))　 　 　 
            argsFound. add(argChar); 　 　 
        else {　 　 　 
            unexpectedArguments. add(argChar); 　 　 　 
            errorCode = ErrorCode.UNEXPECTED_ ARGUMENT; 　 　 　 
            valid = false; 　 　
        }　
    }　 
```
* 处理Integer参数的时候可能的一场未输入对应的参数 e.g. -p -l -d "/etc/profile" 或者e.g. 参数非Integer类型 e.g. -l -p 10.12 -d "/etc/profile", 设置errorCode后抛出一场
```java
private void setIntArg(char argChar)throws ArgsException {　 　 
        currentArgument +  + ; 　 　 
        String parameter = null; 　 　 
        try {　 　 　 
            parameter = args[currentArgument]; 　 　 　 
            intArgs.put(argChar, new Integer(parameter)); 　 　
        }
        catch (ArrayIndexOutOfBoundsException e) {　 　 　 
            valid = false; 　 　 　 
            errorArgumentId = argChar; 　 　 　 
            errorCode = ErrorCode. MISSING_ INTEGER; 　 　 　 
            throw new ArgsException(); 　 　
        }
        catch (NumberFormatException e) {　 　 　 
            valid = false; 　 　 　 
            errorArgumentId = argChar; 　 　 　 
            errorParameter = parameter; 　 　 　 
            errorCode = ErrorCode. INVALID_ INTEGER; 　 　 　 
            throw new ArgsException(); 　 　
        }　
    }
```
* 处理String参数的时候可能出现的为输入对应的参数 e.g. -p 2358 -l -d的情况， 设置errorCode后抛出异常， version2 中美誉抛出异常
```java
    private void setStringArg(char argChar)throws ArgsException {　 　 
        currentArgument +  + ; 　 　 
        try {　 　 　 
            stringArgs.put(argChar, args[currentArgument]); 　 　
        }
        catch (ArrayIndexOutOfBoundsException e) {　 　 　 
            valid = false; 　 　 　 
            errorArgumentId = argChar; 　 　 　 
            errorCode = ErrorCode. MISSING_ STRING; 　 　 　 
            throw new ArgsException(); 　 　
        }　
    }
```
* 这些抛出的异常可以在parse()中通过try catch捕捉到， version 2版本不捕捉这些异常
```java
    private boolean parse() throws ParseException {　 　 
        if (schema. length() == 0 && args. length == 0)　 　 　 
            return true; 
        parseSchema(); 　 　 
        try {　 　 　 
            parseArguments(); 　 　
        }
        catch (ArgsException e) {　 　
        }　 　 
        return valid; 　
    }　 
```
vs
```
    private boolean parse() throws ParseException { 　 　
        if (schema.length() == 0 && args.length == 0) 　 　 　
            return true; 　 　
        parseSchema(); 　 　
        parseArguments(); 　 　
        return valid; 　
    } 　
```
5. 通过在处理Schema和Argument的函数后面增加`throws ArgsException`或者 `throws ParseException`再调用该函数的时候就需要增加try catch捕获，version 2只有Args初始化, parse(), parseSchema(), parseSchemaElement(), validateSchemaElementId这些函数增加这个处理，而且在他们所调用的函数里边没有看到太多异常的抛出
>方法后边加throws XXX 声明该方法将抛出指定异常(Throwable 及其子类)。调用该方法的方法中需对可能抛出的异常做处理，例如使用try catch块包裹，或调用方法也声明向上抛出该异常或异常的父类。方法后不加throws XXX，且其内部并没有trh catch块，则出现异常时默认向上抛出. 

6. unexpectedArgumentMessage()改造成统一通过errorCode来返回不同的错误信息
```java
public String errorMessage() throws Exception {　 　 
        switch (errorCode) {　 　 　 
            case OK:　 　 　 　 
                throw new Exception(" TILT: 　 Should 　 not 　 get 　 here."); 　 　 　 
            case UNEXPECTED_ ARGUMENT:　 　 　 　 
                return unexpectedArgumentMessage(); 　 　 　 
            case MISSING_ STRING:　 　 　 　 
                return String. format(" Could 　 not 　 find 　 string 　 parameter 　 for 　-% c.", errorArgumentId); 　 　 　 
            case INVALID_ INTEGER:　 　 　 　 
                return String. format(" Argument 　-% c 　 expects 　 an 　 integer 　 but 　 was 　'% s'.", errorArgumentId, errorParameter); 　 　 　 
            case MISSING_ INTEGER:　 　 　 　 
                return String. format(" Could 　 not 　 find 　 integer 　 parameter 　 for 　-% c.", errorArgumentId); 　 　
        }
        return ""; 　
    } 　 
    private String unexpectedArgumentMessage() { 　 　 
        StringBuffer message = new StringBuffer(" Argument( s) 　-"); 　 　 
        for (char c : unexpectedArguments) { 　 　 　 
            message. append( c); 　 　
        } 　 　 
        message. append(" 　 unexpected."); 　 　 
        return message. toString(); 　
    } 
```
vs
```java
public String errorMessage() throws Exception { 　 　
        if (unexpectedArguments.size() > 0) { 　 　 　
            return unexpectedArgumentMessage(); 　 　
        } 
        else 　 　 　
        switch (errorCode) { 　 　 　 　
            case MISSING_STRING: 　 　 　 　 　
                return String.format(" Could 　 not 　 find 　 string 　 parameter 　 for 　-% c.", errorArgument); 　 　 　 　
            case OK: 　 　 　 　 　
                throw new Exception(" TILT: 　 Should 　 not 　 get 　 here."); 　 　 　
        } 　 　
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
```
7.  parseSchemaElement()增加了对于Interger类型的schema的处理，增加了parseIntegerSchemaElement()和isIntegerSchemaElement(), 
8. parseElement()的时候， 通过修改setArgument(), setIntArg(), isIntArg()增加对Interger参数的处理
9.  增加了对Interger类型的schema的argument的获取， 和argument是否存在的判断
# Version Final
总体来说，每增加对一个类型的处理， 修改和增加的函数都比较多，且分布比较零散， 所以作者认为代码失去控制了，比较混乱， 因此他开始重构。
>每种 参数 类型 都 需要 在 三个 主要 位置 增加 新 代码。 首先， 每种 参数 类型 都要 有 解析 其 范式 元素、 从而 为 该种 类型 选择 HashMap 的 方法。 其次， 每种 参数 类型 都 需要 在 命令行 字符串 中 解析， 然后 再 转换 为真 实 类型。 最后， 每种 参数 类型 都 需要 一个 getXXX 方法， 按照 其 真实 类型 向 调用 者 返回 参数 值。
>许多 种 不同 类型， 类似 的 方法—— 听起来 像 是个 类。 ArgumentMarshaler 的 概念 就是 这样 产生 的。

parseSchemaElement
setArgument
getString, getgetInt, getBoolean
实际上还有一个地方就是增加对于每种类型的处理的异常信息和捕获

作者希望不破坏功能呢过的情况下进行重构，因此提前准备了一套随需运行的，确保系统行为不会改变的自动化测试。随时可以验证程序行为。

之后的可以参考Args.java和Args_v3.java之间的比较就能知道最终的改动，这章作者想说明的是这种改动是慢慢逐渐完善的


