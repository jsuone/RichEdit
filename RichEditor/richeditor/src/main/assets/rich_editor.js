
var RE = {};
RE.currentSelection = {
    "startContainer": 0,
    "startOffset": 0,
    "endContainer": 0,
    "endOffset": 0};

RE.editor = document.getElementById('editor');

document.addEventListener("selectionchange", function() { RE.backuprange(); });

// Initializations
RE.callback = function() {
    window.location.href = "re-callback://" + encodeURIComponent(RE.getHtml());
}
RE.getHtml = function() {
    return RE.editor.innerHTML;
}
RE.refreshEditingItems = function() {
    RE.enabledEditingItems();
}

RE.setHtml = function(contents) {
    //RE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));//在光标wei'zh位置插入.getHtml = function() {
    document.execCommand('insertHTML', false,  decodeURIComponent(contents.replace(/\+/g, '%20')));
   // return RE.editor.innerHTML;
}

RE.getText = function() {
    return RE.editor.innerText;
}
RE.setText = function(contents) {
    RE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20')); //先把所有+替换为%20(空格符)
}
RE.setBaseTextColor = function(color) {
    RE.editor.style.color  = color;
}

RE.setBaseFontSize = function(size) {
    RE.editor.style.fontSize = size;
}

RE.setPadding = function(left, top, right, bottom) {
  RE.editor.style.paddingLeft = left;
  RE.editor.style.paddingTop = top;
  RE.editor.style.paddingRight = right;
  RE.editor.style.paddingBottom = bottom;
}

RE.setBackgroundColor = function(color) {
    document.body.style.backgroundColor = color;
}

RE.setBackgroundImage = function(image) {
    RE.editor.style.backgroundImage = image;
}

RE.setWidth = function(size) {
    RE.editor.style.minWidth = size;
}

RE.setHeight = function(size) {
    RE.editor.style.height = size;
}

RE.setTextAlign = function(align) {
    RE.editor.style.textAlign = align;
}

RE.setVerticalAlign = function(align) {
    RE.editor.style.verticalAlign = align;
}

RE.setPlaceholder = function(placeholder) {
    RE.editor.setAttribute("placeholder", placeholder);
}

RE.setInputEnabled = function(inputEnabled) {
    RE.editor.contentEditable = String(inputEnabled);
}

RE.undo = function() {
    document.execCommand('undo', false, null);
        RE.enabledEditingItems();
}

RE.redo = function() {
    document.execCommand('redo', false, null);
        RE.enabledEditingItems();
}

RE.setBold = function() {
    document.execCommand('bold', false, null);
     RE.enabledEditingItems();
}

RE.setItalic = function() {
    document.execCommand('italic', false, null);
    RE.enabledEditingItems();
}

RE.setSubscript = function() {
    document.execCommand('subscript', false, null);
}

RE.setSuperscript = function() {
    document.execCommand('superscript', false, null);
}

RE.setStrikeThrough = function() {
    document.execCommand('strikeThrough', false, null);
}

RE.setUnderline = function() {
    document.execCommand('underline', false, null);
}

RE.setBullets = function() {
    document.execCommand('insertUnorderedList', false, null);
}

RE.setNumbers = function() {
    document.execCommand('insertOrderedList', false, null);
}

RE.setTextColor = function(color) {
    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('foreColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RE.setTextBackgroundColor = function(color) {
    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('hiliteColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RE.setFontSize = function(fontSize){
    document.execCommand("fontSize", false, fontSize);
}

RE.setHeading = function(heading) {
    document.execCommand('formatBlock', false, '<h'+heading+'>');
}

RE.setIndent = function() {
    document.execCommand('indent', false, null);
}

RE.setOutdent = function() {
    document.execCommand('outdent', false, null);
}

RE.setJustifyLeft = function() {
    document.execCommand('justifyLeft', false, null);
}

RE.setJustifyCenter = function() {
    document.execCommand('justifyCenter', false, null);
}

RE.setJustifyRight = function() {
    document.execCommand('justifyRight', false, null);
}

RE.setBlockquote = function() {
    document.execCommand('formatBlock', false, '<blockquote>');
}

RE.insertImage = function(url, alt) {
    var html = '<img src="' + url + '" alt="' + alt + '" />';
    RE.insertHTML(html);
}

RE.insertImageW = function(url, alt, width) {
    var html = '<img src="' + url + '" alt="' + alt + '" width="' + width + '"/>';
    RE.insertHTML(html);
}

RE.insertImageWH = function(url, alt, width, height) {
    var html = '<img src="' + url + '" alt="' + alt + '" width="' + width + '" height="' + height +'"/>';
    RE.setHtml(html);
}


RE.insertHTML = function(html) {
    RE.restorerange();
    document.execCommand('insertHTML', false, html);
}

RE.insertLink = function(url, title) {
    RE.restorerange();
    var sel = document.getSelection();
    if (sel.toString().length == 0) {
        document.execCommand("insertHTML",false,"<a href='"+url+"'>"+title+"</a>");
    } else if (sel.rangeCount) {
       var el = document.createElement("a");
       el.setAttribute("href", url);
       el.setAttribute("title", title);

       var range = sel.getRangeAt(0).cloneRange();
       range.surroundContents(el);
       sel.removeAllRanges();
       sel.addRange(range);
   }
    RE.callback();
}

RE.setTodo = function(text) {
    var html = '<input type="checkbox" name="'+ text +'" value="'+ text +'"/> &nbsp;';
    document.execCommand('insertHTML', false, html);
}

RE.prepareInsert = function() {
    RE.backuprange();
}

RE.backuprange = function(){
    var selection = window.getSelection();
    if (selection.rangeCount > 0) {
      var range = selection.getRangeAt(0);
      RE.currentSelection = {
          "startContainer": range.startContainer,
          "startOffset": range.startOffset,
          "endContainer": range.endContainer,
          "endOffset": range.endOffset};
    }
}

RE.restorerange = function(){
    var selection = window.getSelection();
    selection.removeAllRanges();
    var range = document.createRange();
    range.setStart(RE.currentSelection.startContainer, RE.currentSelection.startOffset);
    range.setEnd(RE.currentSelection.endContainer, RE.currentSelection.endOffset);
    selection.addRange(range);
}

RE.enabledEditingItems = function(e) {
    var items = [];
    if (document.queryCommandState('bold')) {
        items.push('bold');
    }
    if (document.queryCommandState('italic')) {
        items.push('italic');
    }
    if (document.queryCommandState('subscript')) {
        items.push('subscript');
    }
    if (document.queryCommandState('superscript')) {
        items.push('superscript');
    }
    if (document.queryCommandState('strikeThrough')) {
        items.push('strikeThrough');
    }
    if (document.queryCommandState('underline')) {
        items.push('underline');
    }
    if (document.queryCommandState('insertOrderedList')) {
        items.push('orderedList');
    }
    if (document.queryCommandState('insertUnorderedList')) {
        items.push('unorderedList');
    }
    if (document.queryCommandState('justifyCenter')) {
        items.push('justifyCenter');
    }
    if (document.queryCommandState('justifyFull')) {
        items.push('justifyFull');
    }
    if (document.queryCommandState('justifyLeft')) {
        items.push('justifyLeft');
    }
    if (document.queryCommandState('justifyRight')) {
        items.push('justifyRight');
    }
    if (document.queryCommandState('insertHorizontalRule')) {
        items.push('horizontalRule');
    }
    var formatBlock = document.queryCommandValue('formatBlock');
    if (formatBlock.length > 0) {
        items.push(formatBlock);
    }

    window.location.href = "re-state://" + encodeURI(items.join(','));
}
// 获取焦点
RE.focus = function() {
    var range = document.createRange();
    range.selectNodeContents(RE.editor);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    RE.editor.focus();
}

RE.blurFocus = function() {
    RE.editor.blur();
}

RE.removeFormat = function() {
    document.execCommand('removeFormat', false, null);
}

// Event Listeners
RE.editor.addEventListener("input", RE.callback);

//这是对键盘左右移动事件的监听，在手机上没有必要
RE.editor.addEventListener("keyup", function(e) {
    var KEY_LEFT = 37, KEY_RIGHT = 39;
    if (e.which == KEY_LEFT || e.which == KEY_RIGHT) {
        RE.enabledEditingItems(e);
    }
});
RE.editor.addEventListener("click", RE.enabledEditingItems);
//获取焦点的监听
RE.editor.addEventListener("focus",function(){
window.location.href = "re-focus://true";
});
RE.editor.addEventListener("blur",function(){
window.location.href = "re-focus://false";
})