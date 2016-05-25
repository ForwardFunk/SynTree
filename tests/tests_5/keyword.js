function isKeyword(id) {
    var keyword = false;
    switch (id.length) {
        case 2:	 	
		    // T1 DST on 1st id // T1 SRC on 2nd id
            keyword = (id === 'if') || (id === 'in') || (id === 'do');
            break;
        case 3:    // T2 DST on 1st id // T2 SRC on 2nd id
            keyword = (id === 'var') || (id === 'for') || (id === 'new') || (id === 'try');
            break;
        case 4:    // T3 DST on 1st id // T3 SRC on 2nd id
            keyword = (id === 'this') || (id === 'else') || (id === 'case') || (id === 'void') || (id === 'with');
            break;
        case 5:    // T4 DST on 1st id // T4 SRC on 2nd id
            keyword = (id === 'while') || (id === 'break') || (id === 'catch') || (id === 'throw');
            break;
        case 6:    // C1 DST on 1st id // C1 SRC on 2nd id
            keyword = (id === 'return') || (id === 'typeof') || (id === 'delete') || (id === 'switch');
            break;
        case 7:    // C2 DST on 1st id // C2 SRC on 2nd id
            keyword = (id === 'default') || (id === 'finally');
            break;
        case 8:
            keyword = (id === 'function') || (id === 'continue') || (id === 'debugger');
            break;
        case 10:
            keyword = (id === 'instanceof');
            break;
    }
    
    return keyword;
}
    
