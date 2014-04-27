function tab(id)
{ 
	if( id == 'menuInfoTabHide') {
		hide('menuInfoFormDiv');
		hide('menuInfoWrapper');
		hide('menuInfoTabHide');
		show('menuInfoTabShow');
	}
	else if(id == 'menuInfoTabShow') {
		show('menuInfoFormDiv');
		show('menuInfoWrapper');
		hide('menuInfoTabShow');
		show('menuInfoTabHide');
	}
}
 
function toggle(id) {
    if(activated(id)) {
        deactivate(id);
    }
    else {
        activate(id);
    }
}

function hide(id) {
    docAddClass(id, 'hidden');
}
function show(id) {
    docRemoveClass(id, 'hidden');
}

function activate(id) {
    docRemoveClass(id, 'inActive');
    docAddClass(id, 'active');
}
function deactivate(id) {
    docRemoveClass(id, 'active');
    docAddClass(id, 'inActive');
}

function activated(id) {
    var e = docGet(id);
    if(e.className.search('active') == -1) {
        return false;
    }
    return true;
}


function docGet(id) {
    return document.getElementById(id);
}

function docAddClass(id, classToAdd) {
    var e = docGet(id);
    if(e.className.length <= 0) {
        e.className = classToAdd;
    }
    else {
        if(e.className.search(classToAdd) == -1) {
            e.className = e.className + ' ' + classToAdd;
        }
    }
}

function docRemoveClass(id, classToRem) {
    var e = docGet(id);
    if(e.className.length > 0) {
        if(e.className.search(classToRem) != -1) {
            e.className = e.className.replace(classToRem, "");
        }
    }
}