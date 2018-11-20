public static void main( String[] args) {
    try {
        Args arg = new Args(" l, p#, d*", args);
        boolean logging = arg.getBoolean(' l');
        intport = arg. getInt(' p');
        Stringdirectory = arg. getString(' d');
        executeApplication( logging, port, directory); 　
    } catch (ArgsException e) {
        System. out. printf(" Argumenterror:% s\ n", e. errorMessage()); 　
    } 
}

//schema  = l, p#, d*
//args = -l -p 2304 -d "/etc/profile"



