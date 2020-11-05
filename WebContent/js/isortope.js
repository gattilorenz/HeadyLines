/**
 * Isotope v1.5.26
 * An exquisite jQuery plugin for magical layouts
 * http://isotope.metafizzy.co
 *
 * Commercial use requires one-time purchase of a commercial license
 * http://isotope.metafizzy.co/docs/license.html
 *
 * Non-commercial use is licensed under the MIT License
 *
 * Copyright 2014 Metafizzy
 */
!function(t,s){"use strict";var i,e=t.document,n=e.documentElement,o=t.Modernizr,r=function(t){return t.charAt(0).toUpperCase()+t.slice(1)},a="Moz Webkit O Ms".split(" "),h=function(t){var s,i=n.style;if("string"==typeof i[t])return t;t=r(t);for(var e=0,o=a.length;o>e;e++)if(s=a[e]+t,"string"==typeof i[s])return s},l=h("transform"),c=h("transitionProperty"),u={csstransforms:function(){return!!l},csstransforms3d:function(){var t=!!h("perspective");if(t&&"webkitPerspective"in n.style){var i=s("<style>@media (transform-3d),(-webkit-transform-3d){#modernizr{height:3px}}</style>").appendTo("head"),e=s('<div id="modernizr" />').appendTo("html");t=3===e.height(),e.remove(),i.remove()}return t},csstransitions:function(){return!!c}};if(o)for(i in u)o.hasOwnProperty(i)||o.addTest(i,u[i]);else{o=t.Modernizr={_version:"1.6ish: miniModernizr for Isotope"};var d,f=" ";for(i in u)d=u[i](),o[i]=d,f+=" "+(d?"":"no-")+i;s("html").addClass(f)}if(o.csstransforms){var p=o.csstransforms3d?{translate:function(t){return"translate3d("+t[0]+"px, "+t[1]+"px, 0) "},scale:function(t){return"scale3d("+t+", "+t+", 1) "}}:{translate:function(t){return"translate("+t[0]+"px, "+t[1]+"px) "},scale:function(t){return"scale("+t+") "}},m=function(t,i,e){var n,o,r=s.data(t,"isoTransform")||{},a={},h={};a[i]=e,s.extend(r,a);for(n in r)o=r[n],h[n]=p[n](o);var c=h.translate||"",u=h.scale||"",d=c+u;s.data(t,"isoTransform",r),t.style[l]=d};s.cssNumber.scale=!0,s.cssHooks.scale={set:function(t,s){m(t,"scale",s)},get:function(t){var i=s.data(t,"isoTransform");return i&&i.scale?i.scale:1}},s.fx.step.scale=function(t){s.cssHooks.scale.set(t.elem,t.now+t.unit)},s.cssNumber.translate=!0,s.cssHooks.translate={set:function(t,s){m(t,"translate",s)},get:function(t){var i=s.data(t,"isoTransform");return i&&i.translate?i.translate:[0,0]}}}var y,g;o.csstransitions&&(y={WebkitTransitionProperty:"webkitTransitionEnd",MozTransitionProperty:"transitionend",OTransitionProperty:"oTransitionEnd otransitionend",transitionProperty:"transitionend"}[c],g=h("transitionDuration"));/*
   * smartresize: debounced resize event for jQuery
   *
   * latest version and complete README available on Github:
   * https://github.com/louisremi/jquery.smartresize.js
   *
   * Copyright 2011 @louis_remi
   * Licensed under the MIT license.
   */
var v,w=s.event,C=s.event.handle?"handle":"dispatch";w.special.smartresize={setup:function(){s(this).bind("resize",w.special.smartresize.handler)},teardown:function(){s(this).unbind("resize",w.special.smartresize.handler)},handler:function(t,s){var i=this,e=arguments;t.type="smartresize",v&&clearTimeout(v),v=setTimeout(function(){w[C].apply(i,e)},"execAsap"===s?0:100)}},s.fn.smartresize=function(t){return t?this.bind("smartresize",t):this.trigger("smartresize",["execAsap"])},s.Isotope=function(t,i,e){this.element=s(i),this._create(t),this._init(e)};var _=["width","height"],A=s(t);s.Isotope.settings={resizable:!0,layoutMode:"masonry",containerClass:"isotope",itemClass:"isotope-item",hiddenClass:"isotope-hidden",hiddenStyle:{opacity:0,scale:.001},visibleStyle:{opacity:1,scale:1},containerStyle:{position:"relative",overflow:"hidden"},animationEngine:"best-available",animationOptions:{queue:!1,duration:800},sortBy:"original-order",sortAscending:!0,resizesContainer:!0,transformsEnabled:!0,itemPositionDataEnabled:!1},s.Isotope.prototype={_create:function(t){this.options=s.extend({},s.Isotope.settings,t),this.styleQueue=[],this.elemCount=0;var i=this.element[0].style;this.originalStyle={};var e=_.slice(0);for(var n in this.options.containerStyle)e.push(n);for(var o=0,r=e.length;r>o;o++)n=e[o],this.originalStyle[n]=i[n]||"";this.element.css(this.options.containerStyle),this._updateAnimationEngine(),this._updateUsingTransforms();var a={"original-order":function(t,s){return s.elemCount++,s.elemCount},random:function(){return Math.random()}};this.options.getSortData=s.extend(this.options.getSortData,a),this.reloadItems(),this.offset={left:parseInt(this.element.css("padding-left")||0,10),top:parseInt(this.element.css("padding-top")||0,10)};var h=this;setTimeout(function(){h.element.addClass(h.options.containerClass)},0),this.options.resizable&&A.bind("smartresize.isotope",function(){h.resize()}),this.element.delegate("."+this.options.hiddenClass,"click",function(){return!1})},_getAtoms:function(t){var s=this.options.itemSelector,i=s?t.filter(s).add(t.find(s)):t,e={position:"absolute"};return i=i.filter(function(t,s){return 1===s.nodeType}),this.usingTransforms&&(e.left=0,e.top=0),i.css(e).addClass(this.options.itemClass),this.updateSortData(i,!0),i},_init:function(t){this.$filteredAtoms=this._filter(this.$allAtoms),this._sort(),this.reLayout(t)},option:function(t){if(s.isPlainObject(t)){this.options=s.extend(!0,this.options,t);var i;for(var e in t)i="_update"+r(e),this[i]&&this[i]()}},_updateAnimationEngine:function(){var t,s=this.options.animationEngine.toLowerCase().replace(/[ _\-]/g,"");switch(s){case"css":case"none":t=!1;break;case"jquery":t=!0;break;default:t=!o.csstransitions}this.isUsingJQueryAnimation=t,this._updateUsingTransforms()},_updateTransformsEnabled:function(){this._updateUsingTransforms()},_updateUsingTransforms:function(){var t=this.usingTransforms=this.options.transformsEnabled&&o.csstransforms&&o.csstransitions&&!this.isUsingJQueryAnimation;t||(delete this.options.hiddenStyle.scale,delete this.options.visibleStyle.scale),this.getPositionStyles=t?this._translate:this._positionAbs},_filter:function(t){var s=""===this.options.filter?"*":this.options.filter;if(!s)return t;var i=this.options.hiddenClass,e="."+i,n=t.filter(e),o=n;if("*"!==s){o=n.filter(s);var r=t.not(e).not(s).addClass(i);this.styleQueue.push({$el:r,style:this.options.hiddenStyle})}return this.styleQueue.push({$el:o,style:this.options.visibleStyle}),o.removeClass(i),t.filter(s)},updateSortData:function(t,i){var e,n,o=this,r=this.options.getSortData;t.each(function(){e=s(this),n={};for(var t in r)n[t]=i||"original-order"!==t?r[t](e,o):s.data(this,"isotope-sort-data")[t];s.data(this,"isotope-sort-data",n)})},_sort:function(){var t=this.options.sortBy,s=this._getSorter,i=this.options.sortAscending?1:-1,e=function(e,n){var o=s(e,t),r=s(n,t);return o===r&&"original-order"!==t&&(o=s(e,"original-order"),r=s(n,"original-order")),(o>r?1:r>o?-1:0)*i};this.$filteredAtoms.sort(e)},_getSorter:function(t,i){return s.data(t,"isotope-sort-data")[i]},_translate:function(t,s){return{translate:[t,s]}},_positionAbs:function(t,s){return{left:t,top:s}},_pushPosition:function(t,s,i){s=Math.round(s+this.offset.left),i=Math.round(i+this.offset.top);var e=this.getPositionStyles(s,i);this.styleQueue.push({$el:t,style:e}),this.options.itemPositionDataEnabled&&t.data("isotope-item-position",{x:s,y:i})},layout:function(t,s){var i=this.options.layoutMode;if(this["_"+i+"Layout"](t),this.options.resizesContainer){var e=this["_"+i+"GetContainerSize"]();this.styleQueue.push({$el:this.element,style:e})}this._processStyleQueue(t,s),this.isLaidOut=!0},_processStyleQueue:function(t,i){var e,n,r,a,h=this.isLaidOut&&this.isUsingJQueryAnimation?"animate":"css",l=this.options.animationOptions,c=this.options.onLayout;if(n=function(t,s){s.$el[h](s.style,l)},this._isInserting&&this.isUsingJQueryAnimation)n=function(t,s){e=s.$el.hasClass("no-transition")?"css":h,s.$el[e](s.style,l)};else if(i||c||l.complete){var u=!1,d=[i,c,l.complete],f=this;if(r=!0,a=function(){if(!u){for(var s,i=0,e=d.length;e>i;i++)s=d[i],"function"==typeof s&&s.call(f.element,t,f);u=!0}},this.isUsingJQueryAnimation&&"animate"===h)l.complete=a,r=!1;else if(o.csstransitions){for(var p,m=0,v=this.styleQueue[0],w=v&&v.$el;!w||!w.length;){if(p=this.styleQueue[m++],!p)return;w=p.$el}var C=parseFloat(getComputedStyle(w[0])[g]);C>0&&(n=function(t,s){s.$el[h](s.style,l).one(y,a)},r=!1)}}s.each(this.styleQueue,n),r&&a(),this.styleQueue=[]},resize:function(){this["_"+this.options.layoutMode+"ResizeChanged"]()&&this.reLayout()},reLayout:function(t){this["_"+this.options.layoutMode+"Reset"](),this.layout(this.$filteredAtoms,t)},addItems:function(t,s){var i=this._getAtoms(t);this.$allAtoms=this.$allAtoms.add(i),s&&s(i)},insert:function(t,s){this.element.append(t);var i=this;this.addItems(t,function(t){var e=i._filter(t);i._addHideAppended(e),i._sort(),i.reLayout(),i._revealAppended(e,s)})},appended:function(t,s){var i=this;this.addItems(t,function(t){i._addHideAppended(t),i.layout(t),i._revealAppended(t,s)})},_addHideAppended:function(t){this.$filteredAtoms=this.$filteredAtoms.add(t),t.addClass("no-transition"),this._isInserting=!0,this.styleQueue.push({$el:t,style:this.options.hiddenStyle})},_revealAppended:function(t,s){var i=this;setTimeout(function(){t.removeClass("no-transition"),i.styleQueue.push({$el:t,style:i.options.visibleStyle}),i._isInserting=!1,i._processStyleQueue(t,s)},10)},reloadItems:function(){this.$allAtoms=this._getAtoms(this.element.children())},remove:function(t,s){this.$allAtoms=this.$allAtoms.not(t),this.$filteredAtoms=this.$filteredAtoms.not(t);var i=this,e=function(){t.remove(),s&&s.call(i.element)};t.filter(":not(."+this.options.hiddenClass+")").length?(this.styleQueue.push({$el:t,style:this.options.hiddenStyle}),this._sort(),this.reLayout(e)):e()},shuffle:function(t){this.updateSortData(this.$allAtoms),this.options.sortBy="random",this._sort(),this.reLayout(t)},destroy:function(){var t=this.usingTransforms,s=this.options;this.$allAtoms.removeClass(s.hiddenClass+" "+s.itemClass).each(function(){var s=this.style;s.position="",s.top="",s.left="",s.opacity="",t&&(s[l]="")});var i=this.element[0].style;for(var e in this.originalStyle)i[e]=this.originalStyle[e];this.element.unbind(".isotope").undelegate("."+s.hiddenClass,"click").removeClass(s.containerClass).removeData("isotope"),A.unbind(".isotope")},_getSegments:function(t){var s,i=this.options.layoutMode,e=t?"rowHeight":"columnWidth",n=t?"height":"width",o=t?"rows":"cols",a=this.element[n](),h=this.options[i]&&this.options[i][e]||this.$filteredAtoms["outer"+r(n)](!0)||a;s=Math.floor(a/h),s=Math.max(s,1),this[i][o]=s,this[i][e]=h},_checkIfSegmentsChanged:function(t){var s=this.options.layoutMode,i=t?"rows":"cols",e=this[s][i];return this._getSegments(t),this[s][i]!==e},_masonryReset:function(){this.masonry={},this._getSegments();var t=this.masonry.cols;for(this.masonry.colYs=[];t--;)this.masonry.colYs.push(0)},_masonryLayout:function(t){var i=this,e=i.masonry;t.each(function(){var t=s(this),n=Math.ceil(t.outerWidth(!0)/e.columnWidth);if(n=Math.min(n,e.cols),1===n)i._masonryPlaceBrick(t,e.colYs);else{var o,r,a=e.cols+1-n,h=[];for(r=0;a>r;r++)o=e.colYs.slice(r,r+n),h[r]=Math.max.apply(Math,o);i._masonryPlaceBrick(t,h)}})},_masonryPlaceBrick:function(t,s){for(var i=Math.min.apply(Math,s),e=0,n=0,o=s.length;o>n;n++)if(s[n]===i){e=n;break}var r=this.masonry.columnWidth*e,a=i;this._pushPosition(t,r,a);var h=i+t.outerHeight(!0),l=this.masonry.cols+1-o;for(n=0;l>n;n++)this.masonry.colYs[e+n]=h},_masonryGetContainerSize:function(){var t=Math.max.apply(Math,this.masonry.colYs);return{height:t}},_masonryResizeChanged:function(){return this._checkIfSegmentsChanged()},_fitRowsReset:function(){this.fitRows={x:0,y:0,height:0}},_fitRowsLayout:function(t){var i=this,e=this.element.width(),n=this.fitRows;t.each(function(){var t=s(this),o=t.outerWidth(!0),r=t.outerHeight(!0);0!==n.x&&o+n.x>e&&(n.x=0,n.y=n.height),i._pushPosition(t,n.x,n.y),n.height=Math.max(n.y+r,n.height),n.x+=o})},_fitRowsGetContainerSize:function(){return{height:this.fitRows.height}},_fitRowsResizeChanged:function(){return!0},_cellsByRowReset:function(){this.cellsByRow={index:0},this._getSegments(),this._getSegments(!0)},_cellsByRowLayout:function(t){var i=this,e=this.cellsByRow;t.each(function(){var t=s(this),n=e.index%e.cols,o=Math.floor(e.index/e.cols),r=(n+.5)*e.columnWidth-t.outerWidth(!0)/2,a=(o+.5)*e.rowHeight-t.outerHeight(!0)/2;i._pushPosition(t,r,a),e.index++})},_cellsByRowGetContainerSize:function(){return{height:Math.ceil(this.$filteredAtoms.length/this.cellsByRow.cols)*this.cellsByRow.rowHeight+this.offset.top}},_cellsByRowResizeChanged:function(){return this._checkIfSegmentsChanged()},_straightDownReset:function(){this.straightDown={y:0}},_straightDownLayout:function(t){var i=this;t.each(function(){var t=s(this);i._pushPosition(t,0,i.straightDown.y),i.straightDown.y+=t.outerHeight(!0)})},_straightDownGetContainerSize:function(){return{height:this.straightDown.y}},_straightDownResizeChanged:function(){return!0},_masonryHorizontalReset:function(){this.masonryHorizontal={},this._getSegments(!0);var t=this.masonryHorizontal.rows;for(this.masonryHorizontal.rowXs=[];t--;)this.masonryHorizontal.rowXs.push(0)},_masonryHorizontalLayout:function(t){var i=this,e=i.masonryHorizontal;t.each(function(){var t=s(this),n=Math.ceil(t.outerHeight(!0)/e.rowHeight);if(n=Math.min(n,e.rows),1===n)i._masonryHorizontalPlaceBrick(t,e.rowXs);else{var o,r,a=e.rows+1-n,h=[];for(r=0;a>r;r++)o=e.rowXs.slice(r,r+n),h[r]=Math.max.apply(Math,o);i._masonryHorizontalPlaceBrick(t,h)}})},_masonryHorizontalPlaceBrick:function(t,s){for(var i=Math.min.apply(Math,s),e=0,n=0,o=s.length;o>n;n++)if(s[n]===i){e=n;break}var r=i,a=this.masonryHorizontal.rowHeight*e;this._pushPosition(t,r,a);var h=i+t.outerWidth(!0),l=this.masonryHorizontal.rows+1-o;for(n=0;l>n;n++)this.masonryHorizontal.rowXs[e+n]=h},_masonryHorizontalGetContainerSize:function(){var t=Math.max.apply(Math,this.masonryHorizontal.rowXs);return{width:t}},_masonryHorizontalResizeChanged:function(){return this._checkIfSegmentsChanged(!0)},_fitColumnsReset:function(){this.fitColumns={x:0,y:0,width:0}},_fitColumnsLayout:function(t){var i=this,e=this.element.height(),n=this.fitColumns;t.each(function(){var t=s(this),o=t.outerWidth(!0),r=t.outerHeight(!0);0!==n.y&&r+n.y>e&&(n.x=n.width,n.y=0),i._pushPosition(t,n.x,n.y),n.width=Math.max(n.x+o,n.width),n.y+=r})},_fitColumnsGetContainerSize:function(){return{width:this.fitColumns.width}},_fitColumnsResizeChanged:function(){return!0},_cellsByColumnReset:function(){this.cellsByColumn={index:0},this._getSegments(),this._getSegments(!0)},_cellsByColumnLayout:function(t){var i=this,e=this.cellsByColumn;t.each(function(){var t=s(this),n=Math.floor(e.index/e.rows),o=e.index%e.rows,r=(n+.5)*e.columnWidth-t.outerWidth(!0)/2,a=(o+.5)*e.rowHeight-t.outerHeight(!0)/2;i._pushPosition(t,r,a),e.index++})},_cellsByColumnGetContainerSize:function(){return{width:Math.ceil(this.$filteredAtoms.length/this.cellsByColumn.rows)*this.cellsByColumn.columnWidth}},_cellsByColumnResizeChanged:function(){return this._checkIfSegmentsChanged(!0)},_straightAcrossReset:function(){this.straightAcross={x:0}},_straightAcrossLayout:function(t){var i=this;t.each(function(){var t=s(this);i._pushPosition(t,i.straightAcross.x,0),i.straightAcross.x+=t.outerWidth(!0)})},_straightAcrossGetContainerSize:function(){return{width:this.straightAcross.x}},_straightAcrossResizeChanged:function(){return!0}},/*!
   * jQuery imagesLoaded plugin v1.1.0
   * http://github.com/desandro/imagesloaded
   *
   * MIT License. by Paul Irish et al.
   */
s.fn.imagesLoaded=function(t){function i(){t.call(n,o)}function e(t){var n=t.target;n.src!==a&&-1===s.inArray(n,h)&&(h.push(n),--r<=0&&(setTimeout(i),o.unbind(".imagesLoaded",e)))}var n=this,o=n.find("img").add(n.filter("img")),r=o.length,a="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==",h=[];return r||i(),o.bind("load.imagesLoaded error.imagesLoaded",e).each(function(){var t=this.src;this.src=a,this.src=t}),n};var S=function(s){t.console&&t.console.error(s)};s.fn.isotope=function(t,i){if("string"==typeof t){var e=Array.prototype.slice.call(arguments,1);this.each(function(){var i=s.data(this,"isotope");return i?s.isFunction(i[t])&&"_"!==t.charAt(0)?void i[t].apply(i,e):void S("no such method '"+t+"' for isotope instance"):void S("cannot call methods on isotope prior to initialization; attempted to call method '"+t+"'")})}else this.each(function(){var e=s.data(this,"isotope");e?(e.option(t),e._init(i)):s.data(this,"isotope",new s.Isotope(t,this,i))});return this}}(window,jQuery);var isortopeNumToString=function(t){t=parseFloat(t)+450359962737;for(var s=40,i=t.toString(),e=Math.floor(Math.log(t)/Math.LN10),n=e;s>n;n++)i="0"+i;return i},isortopeParseString=function(t){t=t.replace(/^\s+|\s+$/g,"");var s=t.split(" ")[0].replace(/[^a-zA-Z0-9\.-]/g,"");return isNaN(parseFloat(s))?t.toLowerCase():isortopeNumToString(s)},isortopeCellFilter=function(t){var s,i=$(t).text().replace(/^\s+|\s+$/g,""),e=$(t).find("input");return s=""!==i?isortopeParseString(i):e.length>0?"on"==e.val()?e.is(":checked").toString():isortopeParseString(e.val()):$(t).html()};jQuery.fn.contentChange=function(t){var s=jQuery(this);return s.each(function(){var s=jQuery(this);s.data("lastContents",s.html()),window.watchContentChange=window.watchContentChange?window.watchContentChange:[],window.watchContentChange.push({element:s,callback:t})}),s},setInterval(function(){if(window.watchContentChange)for(var t in window.watchContentChange)window.watchContentChange[t].element.data("lastContents")!=window.watchContentChange[t].element.html()&&(window.watchContentChange[t].callback.apply(window.watchContentChange[t].element),window.watchContentChange[t].element.data("lastContents",window.watchContentChange[t].element.html()))},500),function(t){"use strict";function s(s,i){this.$el=t(s),this.defaults={autoResort:!0,autoResortInput:!0,autoResortContent:!0},this.opts=t.extend(this.defaults,i,{autoResort:this.$el.data("isortope-autoresort"),autoResortInput:this.$el.data("isortope-autoresort-input"),autoResortContent:this.$el.data("isortope-autoresort-content")}),this.resort=function(){this.$el.data("isortope").sortTable()},this.init(),this.$el.data("isortope",this)}t.fn.isortope=function(i){return this.each("resort"==i?function(){var s=t(this).find("tbody tr");t(this).find("tbody").isotope("updateSortData",s),t(this).data("isortope").resort()}:function(){new s(this,i)})},s.prototype.init=function(){var s=this.$el,i=s.find("tbody");s.css("position","relative"),s.css("height",s.height());for(var e=s.find("th").length,n=0;e>n;n++){var o=s.find("tr:first-child td:nth-child("+(n+1)+")").width();s.find("tr td:nth-child("+(n+1)+")").css("width",o);var r=s.find("th:nth-child("+(n+1)+")"),a=r.width();r.css("width",a),r.css("max-width",a)}var h=i.find("td"),l=h.css("border-top-width"),c=h.css("border-bottom-width"),u=function(){h.css("border-top-width",0),h.css("border-bottom-width",0)},d=function(){h.css("border-top-width",l),h.css("border-bottom-width",c)};if("separate"==s.css("border-collapse")){var f=i.find("tr"),p=parseInt(s.css("border-spacing").split(" ")[0]),m=parseInt(c);f.css("margin-bottom",p+m+"px")}for(var y={},n=0;e>n;n++){var g="col"+n,r=s.find("th:nth-child("+(n+1)+")");if("none"!=r.attr("data-sort-type")){var v="return isortopeCellFilter(item.find('."+g+"'));",w=new Function("item",v);y[g]=w,r.attr("data-sort-type",g),r.css("cursor","pointer"),s.find("tr td:nth-child("+(n+1)+")").addClass(g),s.find("tr td:nth-child("+(n+1)+")").data("sort-type",g)}}u(),i.isotope({itemSelector:"tr",layout:"fitRows",getSortData:y}),d();var C=s.find("thead").height();s.find("tr").css("top",C);var r=s.find("th");r.height(r.height()),r.css("line-height",1);var _=function(){var t=s.find("th.sortAsc,th.sortDesc");t.find(".sort-arrow").remove(),t.removeClass("sortAsc").removeClass("sortDesc")};s.find("th").click(function(){var s=t(this).attr("data-sort-type");if("none"!=s){var i;t(this).hasClass("sortAsc")?(i=!0,_(),t(this).html(t(this).html()+'<span class="sort-arrow">\u25bc</span>'),t(this).addClass("sortDesc")):(i=!1,_(),t(this).html(t(this).html()+'<span class="sort-arrow">\u25b2</span>'),t(this).addClass("sortAsc")),A()}});var A=function(){var e=t(s).find("th.sortAsc, th.sortDesc").closest("th"),n=e.attr("data-sort-type"),o=e.hasClass("sortDesc");u(),i.isotope({sortBy:n,sortAscending:!o}),d(),s.trigger("sort")};this.sortTable=A;var S=function(s){var e=t(s).closest("tr");i.isotope("updateSortData",e);var n=t(s).data("sort-type"),o=t("th[data-sort-type="+n+"]");(o.hasClass("sortAsc")||o.hasClass("sortDesc"))&&A()};this.opts.autoResort&&this.opts.autoResortInput&&s.find("input").change(function(){var s=t(this).parent("td");S(s)}),this.opts.autoResort&&this.opts.autoResortContent&&s.find("td").contentChange(function(){S(this)}),s.trigger("initialized")}}(jQuery,document,window),$(document).ready(function(){$("table.isortope").isortope()}),function(){}.call(this);