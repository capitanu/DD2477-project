(function(){"use strict";var t={4744:function(t,e,n){n(6992),n(8674),n(9601),n(7727);var r=n(8935),o=n(2194),a=n.n(o),i=function(){var t=this,e=t.$createElement,r=t._self._c||e;return r("div",{staticClass:"bg"},[r("div",{staticStyle:{"text-align":"center"},attrs:{id:"app"}},[r("img",{staticStyle:{width:"200px"},attrs:{alt:"Vue logo",src:n(240)}}),r("el-container",{staticClass:"search-and-result",attrs:{direction:"vertical"}},[r("el-container",{staticStyle:{"text-align":"center"}},[r("div",{staticClass:"search-engine-title"},[t._v("Book Search Engine")])]),r("el-container",{staticClass:"search-line",attrs:{direction:"vertical"}},[r("el-row",{attrs:{gutter:20}},[r("el-col",{attrs:{span:21}},[r("el-autocomplete",{staticClass:"autocomplete-display",attrs:{"fetch-suggestions":t.suggest,placeholder:"search any book","trigger-on-focus":!1},on:{select:t.handleSelect},model:{value:t.inp,callback:function(e){t.inp=e},expression:"inp"}})],1),r("el-col",{attrs:{span:3}},[r("el-button",{staticStyle:{width:"100%"},attrs:{type:"primary"},nativeOn:{click:function(e){return t.search.apply(null,arguments)}}},[t._v("Search")])],1)],1)],1),r("el-container",{attrs:{direction:"vertical"}},t._l(t.result.data,(function(e){return r("el-container",{key:e._id},[r("el-container",{staticClass:"result-item"},[r("el-container",{attrs:{direction:"vertical"}},[r("el-row",[r("el-col",{attrs:{span:1}},[r("el-checkbox",{model:{value:e.checked,callback:function(n){t.$set(e,"checked",n)},expression:"item.checked"}})],1),r("el-col",{attrs:{span:23}},[r("div",{staticStyle:{"text-align":"left"}},[r("a",{attrs:{href:"https://www.goodreads.com/book/show/"+e._id}},[t._v(" "+t._s(e._source.title)+" ")])])])],1),r("p",{staticClass:"result-item-summary"},[t._v(t._s(e._source.summary))])],1)],1)],1)})),1)],1)],1)])},c=[],s=n(6198),l=(n(8975),n(7327),n(1539),n(1249),n(6166)),u=n.n(l),d={name:"App",components:{},data:function(){return{inp:"",result:{data:[]}}},methods:{search:function(){var t=(0,s.Z)(regeneratorRuntime.mark((function t(){var e,n;return regeneratorRuntime.wrap((function(t){while(1)switch(t.prev=t.next){case 0:if(n=this.result.data.filter((function(t){return t.checked})),0!=n.length){t.next=9;break}return t.next=4,u()({method:"post",url:"http://localhost:8092/query",data:{title:this.inp}});case 4:e=t.sent,console.log(e),this.result.data=e.data.hits,t.next=15;break;case 9:return console.log(n),t.next=12,u()({method:"post",url:"http://localhost:8092/recommend",data:{titles:n.map((function(t){return t._source.title}))}});case 12:e=t.sent,console.log(e),this.result.data=e.data.hits.map((function(t){return{_id:t.docId,_source:{title:t.title,summary:t.summary,genre:t.genre}}}));case 15:case"end":return t.stop()}}),t,this)})));function e(){return t.apply(this,arguments)}return e}(),suggest:function(){var t=(0,s.Z)(regeneratorRuntime.mark((function t(e,n){var r,o,a;return regeneratorRuntime.wrap((function(t){while(1)switch(t.prev=t.next){case 0:return t.next=2,u()({method:"post",url:"http://localhost:8092/suggest",data:{title:e}});case 2:for(r=t.sent,console.log(r,n),o=r.data[0].options,a=0;a<o.length;a++)o[a].value=o[a].text,o[a].checked=!1;n(o);case 7:case"end":return t.stop()}}),t)})));function e(e,n){return t.apply(this,arguments)}return e}(),handleSelect:function(){}},mounted:function(t){console.log(t)}},f=d,p=n(1001),h=(0,p.Z)(f,i,c,!1,null,null,null),g=h.exports;r.Z.use(a()),r.Z.config.productionTip=!1,new r.Z({render:function(t){return t(g)}}).$mount("#app")},240:function(t,e,n){t.exports=n.p+"img/book.dae4736a.png"}},e={};function n(r){var o=e[r];if(void 0!==o)return o.exports;var a=e[r]={id:r,loaded:!1,exports:{}};return t[r](a,a.exports,n),a.loaded=!0,a.exports}n.m=t,function(){var t=[];n.O=function(e,r,o,a){if(!r){var i=1/0;for(u=0;u<t.length;u++){r=t[u][0],o=t[u][1],a=t[u][2];for(var c=!0,s=0;s<r.length;s++)(!1&a||i>=a)&&Object.keys(n.O).every((function(t){return n.O[t](r[s])}))?r.splice(s--,1):(c=!1,a<i&&(i=a));if(c){t.splice(u--,1);var l=o();void 0!==l&&(e=l)}}return e}a=a||0;for(var u=t.length;u>0&&t[u-1][2]>a;u--)t[u]=t[u-1];t[u]=[r,o,a]}}(),function(){n.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return n.d(e,{a:e}),e}}(),function(){n.d=function(t,e){for(var r in e)n.o(e,r)&&!n.o(t,r)&&Object.defineProperty(t,r,{enumerable:!0,get:e[r]})}}(),function(){n.g=function(){if("object"===typeof globalThis)return globalThis;try{return this||new Function("return this")()}catch(t){if("object"===typeof window)return window}}()}(),function(){n.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)}}(),function(){n.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})}}(),function(){n.nmd=function(t){return t.paths=[],t.children||(t.children=[]),t}}(),function(){n.p="/"}(),function(){var t={143:0};n.O.j=function(e){return 0===t[e]};var e=function(e,r){var o,a,i=r[0],c=r[1],s=r[2],l=0;if(i.some((function(e){return 0!==t[e]}))){for(o in c)n.o(c,o)&&(n.m[o]=c[o]);if(s)var u=s(n)}for(e&&e(r);l<i.length;l++)a=i[l],n.o(t,a)&&t[a]&&t[a][0](),t[a]=0;return n.O(u)},r=self["webpackChunkbook"]=self["webpackChunkbook"]||[];r.forEach(e.bind(null,0)),r.push=e.bind(null,r.push.bind(r))}();var r=n.O(void 0,[998],(function(){return n(4744)}));r=n.O(r)})();
//# sourceMappingURL=app-legacy.84309737.js.map