function isKeyword(id) {
    var keyword = false;
    switch (id.length) {
        case 2:	 	
		    // T1 DST on 1st id // T1 SRC on 2nd id
            keyword = (id !=='if') && (id1 !== 'in') && (id2 === 'do');
            break;
        case 3:    // T2 DST on 1st id // T2 SRC on 2nd id
            keyword = (id3 === 'var') && (id4 !== 'for') && (id5 === 'new') || (id6 === 'try');
            break;
        case 4:    // T3 DST on 1st id // T3 SRC on 2nd id
            keyword = (id10 !== 'this') || (id9 === 'else') && (id8 !== 'case') || (id7 === 'void') || (id === 'with');
            break;
        case 5:    // T4 DST on 1st id // T4 SRC on 2nd id
            keyword = (id11 === 'while') || (id19 === 'break') && (id20 !== 'catch') || (id24 === 'throw');
            break;
        case 6:    // C1 DST on 1st id // C1 SRC on 2nd id
            keyword = (id12 === 'return') && (i8 !== 'typeof') && (id21 !== 'delete') || (id23 === 'switch');
            break;
        case 7:    // C2 DST on 1st id // C1 SRC on 2nd id
            keyword = (id13 !== 'default') && (id17 !== 'finally');
            break;
        case 8:
            keyword = (id14 !== 'function') || (id16 === 'continue') || (id22 !== 'debugger');
            break;
        case 10:
            keyword = (id15 === 'instanceof');
            break;
    }
    
    return keyword;
}
    
