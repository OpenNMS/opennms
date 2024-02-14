/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
function gwtterminal(){var N='',ub='" for "gwt:onLoadErrorFn"',sb='" for "gwt:onPropertyErrorFn"',gb='"><\/script>',X='#',Sb='.cache.html',Z='/',Rb=':',mb='::',$b='<script defer="defer">gwtterminal.onInjectionDone(\'gwtterminal\')<\/script>',fb='<script id="',pb='=',Y='?',Db='ActiveXObject',rb='Bad handler "',Eb='ChromeTab.ChromeFrame',Zb='DOMContentLoaded',Qb="GWT module 'gwtterminal' may need to be (re)compiled",hb='SCRIPT',eb='__gwt_marker_gwtterminal',ib='base',ab='baseUrl',R='begin',Q='bootstrap',Cb='chromeframe',_='clear.cache.gif',ob='content',W='end',Kb='gecko',Lb='gecko1_8',S='gwt.codesvr=',T='gwt.hosted=',U='gwt.hybrid',Tb='gwt/clean/clean.css',tb='gwt:onLoadErrorFn',qb='gwt:onPropertyErrorFn',nb='gwt:property',O='gwtterminal',cb='gwtterminal.nocache.js',lb='gwtterminal::',Yb='head',Ob='hosted.html?gwtterminal',Xb='href',Jb='ie6',Ib='ie8',Hb='ie9',vb='iframe',$='img',wb="javascript:''",Ub='link',Nb='loadExternalRefs',jb='meta',yb='moduleRequested',V='moduleStartup',Gb='msie',kb='name',Ab='opera',xb='position:absolute;width:0;height:0;border:none',Vb='rel',Fb='safari',bb='script',Pb='selectingPermutation',P='startup',Wb='stylesheet',db='undefined',Mb='unknown',zb='user.agent',Bb='webkit';var l=window,m=document,n=l.__gwtStatsEvent?function(a){return l.__gwtStatsEvent(a)}:null,o=l.__gwtStatsSessionId?l.__gwtStatsSessionId:null,p,q,r,s=N,t={},u=[],v=[],w=[],x=0,y,z;n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Q,millis:(new Date).getTime(),type:R});if(!l.__gwt_stylesLoaded){l.__gwt_stylesLoaded={}}if(!l.__gwt_scriptsLoaded){l.__gwt_scriptsLoaded={}}function A(){var b=false;try{var c=l.location.search;return (c.indexOf(S)!=-1||(c.indexOf(T)!=-1||l.external&&l.external.gwtOnLoad))&&c.indexOf(U)==-1}catch(a){}A=function(){return b};return b}
function B(){if(p&&q){var b=m.getElementById(O);var c=b.contentWindow;if(A()){c.__gwt_getProperty=function(a){return F(a)}}gwtterminal=null;c.gwtOnLoad(y,O,s,x);n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:V,millis:(new Date).getTime(),type:W})}}
function C(){function e(a){var b=a.lastIndexOf(X);if(b==-1){b=a.length}var c=a.indexOf(Y);if(c==-1){c=a.length}var d=a.lastIndexOf(Z,Math.min(c,b));return d>=0?a.substring(0,d+1):N}
function f(a){if(a.match(/^\w+:\/\//)){}else{var b=m.createElement($);b.src=a+_;a=e(b.src)}return a}
function g(){var a=E(ab);if(a!=null){return a}return N}
function h(){var a=m.getElementsByTagName(bb);for(var b=0;b<a.length;++b){if(a[b].src.indexOf(cb)!=-1){return e(a[b].src)}}return N}
function i(){var a;if(typeof isBodyLoaded==db||!isBodyLoaded()){var b=eb;var c;m.write(fb+b+gb);c=m.getElementById(b);a=c&&c.previousSibling;while(a&&a.tagName!=hb){a=a.previousSibling}if(c){c.parentNode.removeChild(c)}if(a&&a.src){return e(a.src)}}return N}
function j(){var a=m.getElementsByTagName(ib);if(a.length>0){return a[a.length-1].href}return N}
var k=g();if(k==N){k=h()}if(k==N){k=i()}if(k==N){k=j()}if(k==N){k=e(m.location.href)}k=f(k);s=k;return k}
function D(){var b=document.getElementsByTagName(jb);for(var c=0,d=b.length;c<d;++c){var e=b[c],f=e.getAttribute(kb),g;if(f){f=f.replace(lb,N);if(f.indexOf(mb)>=0){continue}if(f==nb){g=e.getAttribute(ob);if(g){var h,i=g.indexOf(pb);if(i>=0){f=g.substring(0,i);h=g.substring(i+1)}else{f=g;h=N}t[f]=h}}else if(f==qb){g=e.getAttribute(ob);if(g){try{z=eval(g)}catch(a){alert(rb+g+sb)}}}else if(f==tb){g=e.getAttribute(ob);if(g){try{y=eval(g)}catch(a){alert(rb+g+ub)}}}}}}
function E(a){var b=t[a];return b==null?null:b}
function F(a){var b=v[a](),c=u[a];if(b in c){return b}var d=[];for(var e in c){d[c[e]]=e}if(z){z(a,d,b)}throw null}
var G;function H(){if(!G){G=true;var a=m.createElement(vb);a.src=wb;a.id=O;a.style.cssText=xb;a.tabIndex=-1;m.body.appendChild(a);n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:V,millis:(new Date).getTime(),type:yb});a.contentWindow.location.replace(s+J)}}
v[zb]=function(){var c=navigator.userAgent.toLowerCase();var d=function(a){return parseInt(a[1])*1000+parseInt(a[2])};if(function(){return c.indexOf(Ab)!=-1}())return Ab;if(function(){return c.indexOf(Bb)!=-1||function(){if(c.indexOf(Cb)!=-1){return true}if(typeof window[Db]!=db){try{var b=new ActiveXObject(Eb);if(b){b.registerBhoIfNeeded();return true}}catch(a){}}return false}()}())return Fb;if(function(){return c.indexOf(Gb)!=-1&&m.documentMode>=9}())return Hb;if(function(){return c.indexOf(Gb)!=-1&&m.documentMode>=8}())return Ib;if(function(){var a=/msie ([0-9]+)\.([0-9]+)/.exec(c);if(a&&a.length==3)return d(a)>=6000}())return Jb;if(function(){return c.indexOf(Kb)!=-1}())return Lb;return Mb};u[zb]={gecko1_8:0,ie6:1,ie8:2,ie9:3,opera:4,safari:5};gwtterminal.onScriptLoad=function(){if(G){q=true;B()}};gwtterminal.onInjectionDone=function(){p=true;n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Nb,millis:(new Date).getTime(),type:W});B()};D();C();var I;var J;if(A()){if(l.external&&(l.external.initModule&&l.external.initModule(O))){l.location.reload();return}J=Ob;I=N}n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Q,millis:(new Date).getTime(),type:Pb});if(!A()){try{alert(Qb);return;var K=I.indexOf(Rb);if(K!=-1){x=Number(I.substring(K+1));I=I.substring(0,K)}J=I+Sb}catch(a){return}}var L;function M(){if(!r){r=true;if(!__gwt_stylesLoaded[Tb]){var a=m.createElement(Ub);__gwt_stylesLoaded[Tb]=a;a.setAttribute(Vb,Wb);a.setAttribute(Xb,s+Tb);m.getElementsByTagName(Yb)[0].appendChild(a)}B();if(m.removeEventListener){m.removeEventListener(Zb,M,false)}if(L){clearInterval(L)}}}
if(m.addEventListener){m.addEventListener(Zb,function(){H();M()},false)}var L=setInterval(function(){if(/loaded|complete/.test(m.readyState)){H();M()}},50);n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Q,millis:(new Date).getTime(),type:W});n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Nb,millis:(new Date).getTime(),type:R});m.write($b)}
gwtterminal();