
	function onBlur(ele, searchValue) {
		if (ele.value == '') { 
			ele.value = searchValue;
		 	ele.className += " sheptFieldBlur";
		 }
	}
	
	function onFocus(ele, searchValue) {
		if (ele.value == searchValue) {
			ele.value = ''; 
			ele.className = ele.className.replace(/\bsheptFieldBlur\b/,'');
		}
	}

