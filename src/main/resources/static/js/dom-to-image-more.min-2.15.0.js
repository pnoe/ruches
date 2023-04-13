/*! dom-to-image-more 01-02-2023 */
!function(n){"use strict";const s=function(){let t=0;return{escape:function(t){return t.replace(/([.*+?^${}()|\[\]\/\\])/g,"\\$1")},isDataUrl:function(t){return-1!==t.search(/^(data:)/)},canvasToBlob:function(e){if(e.toBlob)return new Promise(function(t){e.toBlob(t)});return function(o){return new Promise(function(t){var e=l(o.toDataURL().split(",")[1]),n=e.length,r=new Uint8Array(n);for(let t=0;t<n;t++)r[t]=e.charCodeAt(t);t(new Blob([r],{type:"image/png"}))})}(e)},resolveUrl:function(t,e){var n=document.implementation.createHTMLDocument(),r=n.createElement("base"),o=(n.head.appendChild(r),n.createElement("a"));return n.body.appendChild(o),r.href=e,o.href=t,o.href},getAndEncode:function(u){let t=c.impl.urlCache.find(function(t){return t.url===u});t||(t={url:u,promise:null},c.impl.urlCache.push(t));null===t.promise&&(c.impl.options.cacheBust&&(u+=(/\?/.test(u)?"&":"?")+(new Date).getTime()),t.promise=new Promise(function(e){const t=c.impl.options.httpTimeout,n=new XMLHttpRequest;n.onreadystatechange=function(){if(4===n.readyState)if(200!==n.status)r?e(r):i(`cannot fetch resource: ${u}, status: `+n.status);else{const t=new FileReader;t.onloadend=function(){e(t.result)},t.readAsDataURL(n.response)}},n.ontimeout=function(){r?e(r):i(`timeout of ${t}ms occured while fetching resource: `+u)},n.responseType="blob",n.timeout=t,c.impl.options.useCredentials&&(n.withCredentials=!0),n.open("GET",u,!0),n.send();let r;var o;function i(t){console.error(t),e("")}c.impl.options.imagePlaceholder&&(o=c.impl.options.imagePlaceholder.split(/,/))&&o[1]&&(r=o[1])}));return t.promise},uid:function(){return"u"+("0000"+(Math.random()*Math.pow(36,4)<<0).toString(36)).slice(-4)+t++},delay:function(n){return function(e){return new Promise(function(t){setTimeout(function(){t(e)},n)})}},asArray:function(e){var n=[],r=e.length;for(let t=0;t<r;t++)n.push(e[t]);return n},escapeXhtml:function(t){return t.replace(/%/g,"%25").replace(/#/g,"%23").replace(/\n/g,"%0A")},makeImage:function(r){return"data:,"!==r?new Promise(function(t,e){const n=new Image;c.impl.options.useCredentials&&(n.crossOrigin="use-credentials"),n.onload=function(){t(n)},n.onerror=e,n.src=r}):Promise.resolve()},width:function(t){var e=r(t,"border-left-width"),n=r(t,"border-right-width");return t.scrollWidth+e+n},height:function(t){var e=r(t,"border-top-width"),n=r(t,"border-bottom-width");return t.scrollHeight+e+n},getWindow:e,isElement:function(t){return t instanceof e(t).Element},isHTMLElement:function(t){return t instanceof e(t).HTMLElement},isHTMLCanvasElement:function(t){return t instanceof e(t).HTMLCanvasElement},isHTMLInputElement:function(t){return t instanceof e(t).HTMLInputElement},isHTMLImageElement:function(t){return t instanceof e(t).HTMLImageElement},isHTMLTextAreaElement:function(t){return t instanceof e(t).HTMLTextAreaElement},isSVGElement:function(t){return t instanceof e(t).SVGElement},isSVGRectElement:function(t){return t instanceof e(t).SVGRectElement}};function e(t){t=t?t.ownerDocument:void 0;return(t?t.defaultView:void 0)||n||window}function r(t,e){t=f(t).getPropertyValue(e);return parseFloat(t.replace("px",""))}}(),o=function(){const r=/url\(['"]?([^'"]+?)['"]?\)/g;return{inlineAll:function(e,r,o){if(!t(e))return Promise.resolve(e);return Promise.resolve(e).then(n).then(function(t){let n=Promise.resolve(e);return t.forEach(function(e){n=n.then(function(t){return i(t,e,r,o)})}),n})},shouldProcess:t,impl:{readUrls:n,inline:i}};function t(t){return-1!==t.search(r)}function n(t){for(var e,n=[];null!==(e=r.exec(t));)n.push(e[1]);return n.filter(function(t){return!s.isDataUrl(t)})}function i(n,r,e,t){return Promise.resolve(r).then(function(t){return e?s.resolveUrl(t,e):t}).then(t||s.getAndEncode).then(function(t){return n.replace((e=r,new RegExp(`(url\\(['"]?)(${s.escape(e)})(['"]?\\))`,"g")),`$1${t}$3`);var e})}}(),t={resolveAll:function(){return e().then(function(t){return Promise.all(t.map(function(t){return t.resolve()}))}).then(function(t){return t.join("\n")})},impl:{readAll:e}};function e(){return Promise.resolve(s.asArray(document.styleSheets)).then(function(t){const n=[];return t.forEach(function(e){if(Object.getPrototypeOf(e).hasOwnProperty("cssRules"))try{s.asArray(e.cssRules||[]).forEach(n.push.bind(n))}catch(t){console.log("Error while reading CSS rules from "+e.href,t.toString())}}),n}).then(function(t){return t.filter(function(t){return t.type===CSSRule.FONT_FACE_RULE}).filter(function(t){return o.shouldProcess(t.style.getPropertyValue("src"))})}).then(function(t){return t.map(e)});function e(e){return{resolve:function(){var t=(e.parentStyleSheet||{}).href;return o.inlineAll(e.cssText,t)},src:function(){return e.style.getPropertyValue("src")}}}}const r={inlineAll:function e(t){if(!s.isElement(t))return Promise.resolve(t);return n(t).then(function(){return s.isHTMLImageElement(t)?i(t).inline():Promise.all(s.asArray(t.childNodes).map(function(t){return e(t)}))});function n(r){const t=["background","background-image"],e=t.map(function(e){const t=r.style.getPropertyValue(e),n=r.style.getPropertyPriority(e);return t?o.inlineAll(t).then(function(t){r.style.setProperty(e,t,n)}):Promise.resolve()});return Promise.all(e).then(function(){return r})}},impl:{newImage:i}};function i(n){return{inline:function(t){if(s.isDataUrl(n.src))return Promise.resolve();return Promise.resolve(n.src).then(t||s.getAndEncode).then(function(e){return new Promise(function(t){n.onload=t,n.onerror=t,n.src=e})})}}}const u={imagePlaceholder:void 0,cacheBust:!1,useCredentials:!1,httpTimeout:3e4},c={toSvg:a,toPng:function(t,e){return h(t,e).then(function(t){return t.toDataURL()})},toJpeg:function(t,e){return h(t,e).then(function(t){return t.toDataURL("image/jpeg",(e?e.quality:void 0)||1)})},toBlob:function(t,e){return h(t,e).then(s.canvasToBlob)},toPixelData:function(e,t){return h(e,t).then(function(t){return t.getContext("2d").getImageData(0,0,s.width(e),s.height(e)).data})},toCanvas:h,impl:{fontFaces:t,images:r,util:s,inliner:o,urlCache:[],options:{}}},f=("object"==typeof exports&&"object"==typeof module?module.exports=c:n.domtoimage=c,n.getComputedStyle||window.getComputedStyle),l=n.atob||window.atob;function a(r,o){const e=c.impl.util.getWindow(r);var t=o=o||{};return void 0===t.imagePlaceholder?c.impl.options.imagePlaceholder=u.imagePlaceholder:c.impl.options.imagePlaceholder=t.imagePlaceholder,void 0===t.cacheBust?c.impl.options.cacheBust=u.cacheBust:c.impl.options.cacheBust=t.cacheBust,void 0===t.useCredentials?c.impl.options.useCredentials=u.useCredentials:c.impl.options.useCredentials=t.useCredentials,void 0===t.httpTimeout?c.impl.options.httpTimeout=u.httpTimeout:c.impl.options.httpTimeout=t.httpTimeout,Promise.resolve(r).then(function(t){return function i(e,u,o,c,l){if(!o&&u&&!u(e))return Promise.resolve();return Promise.resolve(e).then(t).then(function(t){return n(e,t)}).then(function(t){return r(e,t)});function t(t){return s.isHTMLCanvasElement(t)?s.makeImage(t.toDataURL()):t.cloneNode(!1)}function n(o,t){const e=o.childNodes;return 0===e.length?Promise.resolve(t):n(t,s.asArray(e)).then(function(){return t});function n(e,t){const n=f(o);let r=Promise.resolve();return t.forEach(function(t){r=r.then(function(){return i(t,u,!1,n,l)}).then(function(t){t&&e.appendChild(t)})}),r}}function r(l,a){return s.isElement(a)?Promise.resolve().then(t).then(e).then(n).then(r).then(function(){return a}):a;function t(){function r(t,e){e.font=t.font,e.fontFamily=t.fontFamily,e.fontFeatureSettings=t.fontFeatureSettings,e.fontKerning=t.fontKerning,e.fontSize=t.fontSize,e.fontStretch=t.fontStretch,e.fontStyle=t.fontStyle,e.fontVariant=t.fontVariant,e.fontVariantCaps=t.fontVariantCaps,e.fontVariantEastAsian=t.fontVariantEastAsian,e.fontVariantLigatures=t.fontVariantLigatures,e.fontVariantNumeric=t.fontVariantNumeric,e.fontVariationSettings=t.fontVariationSettings,e.fontWeight=t.fontWeight}function t(t,e){const n=f(t);n.cssText?(e.style.cssText=n.cssText,r(n,e.style)):(p(n,c,e),o&&(["inset-block","inset-block-start","inset-block-end"].forEach(t=>e.style.removeProperty(t)),["left","right","top","bottom"].forEach(t=>{e.style.getPropertyValue(t)&&e.style.setProperty(t,"0px")})))}t(l,a)}function e(){const c=s.uid();function e(o){const i=f(l,o),u=i.getPropertyValue("content");if(""!==u&&"none"!==u){const e=a.getAttribute("class")||"",n=(a.setAttribute("class",e+" "+c),document.createElement("style"));function t(){const t=`.${c}:`+o,e=(i.cssText?n:r)();return document.createTextNode(t+`{${e}}`);function n(){return`${i.cssText} content: ${u};`}function r(){const t=s.asArray(i).map(e).join("; ");return t+";";function e(t){const e=i.getPropertyValue(t),n=i.getPropertyPriority(t)?" !important":"";return t+": "+e+n}}}n.appendChild(t()),a.appendChild(n)}}[":before",":after"].forEach(function(t){e(t)})}function n(){s.isHTMLTextAreaElement(l)&&(a.innerHTML=l.value),s.isHTMLInputElement(l)&&a.setAttribute("value",l.value)}function r(){s.isSVGElement(a)&&(a.setAttribute("xmlns","http://www.w3.org/2000/svg"),s.isSVGRectElement(a))&&["width","height"].forEach(function(t){const e=a.getAttribute(t);e&&a.style.setProperty(t,e)})}}}(t,o.filter,!0,null,e)}).then(m).then(d).then(function(e){o.bgcolor&&(e.style.backgroundColor=o.bgcolor);o.width&&(e.style.width=o.width+"px");o.height&&(e.style.height=o.height+"px");o.style&&Object.keys(o.style).forEach(function(t){e.style[t]=o.style[t]});let t=null;"function"==typeof o.onclone&&(t=o.onclone(e));return Promise.resolve(t).then(function(){return e})}).then(function(t){return t=t,e=o.width||s.width(r),n=o.height||s.height(r),Promise.resolve(t).then(function(t){return t.setAttribute("xmlns","http://www.w3.org/1999/xhtml"),(new XMLSerializer).serializeToString(t)}).then(s.escapeXhtml).then(function(t){return`<foreignObject x="0" y="0" width="100%" height="100%">${t}</foreignObject>`}).then(function(t){return`<svg xmlns="http://www.w3.org/2000/svg" width="${e}" height="${n}">${t}</svg>`}).then(function(t){return"data:image/svg+xml;charset=utf-8,"+t});var e,n}).then(function(t){return c.impl.urlCache=[],function(){y&&(document.body.removeChild(y),y=null);g&&clearTimeout(g);g=setTimeout(()=>{g=null,v={}},2e4)}(),t})}function h(o,i){return a(o,i=i||{}).then(s.makeImage).then(s.delay(0)).then(function(t){var e="number"!=typeof i.scale?1:i.scale,n=function(t,e){var n=document.createElement("canvas");n.width=(i.width||s.width(t))*e,n.height=(i.height||s.height(t))*e,i.bgcolor&&((t=n.getContext("2d")).fillStyle=i.bgcolor,t.fillRect(0,0,n.width,n.height));return n}(o,e),r=n.getContext("2d");return r.mozImageSmoothingEnabled=!1,r.msImageSmoothingEnabled=!1,r.imageSmoothingEnabled=!1,t&&(r.scale(e,e),r.drawImage(t,0,0)),n})}function m(n){return t.resolveAll().then(function(t){var e;return""!==t&&(e=document.createElement("style"),n.appendChild(e),e.appendChild(document.createTextNode(t))),n})}function d(t){return r.inlineAll(t).then(function(){return t})}function p(i,u,t){const c=function(t){if(v[t])return v[t];var e=function(){if(y)return y.contentWindow;var t=document.characterSet||"UTF-8",e=document.doctype,e=e?(`<!DOCTYPE ${n(e.name)} ${n(e.publicId)} `+n(e.systemId)).trim()+">":"";return(y=document.createElement("iframe")).id="domtoimage-sandbox-"+s.uid(),y.style.visibility="hidden",y.style.position="fixed",document.body.appendChild(y),function(t,e,n,r){try{return t.contentWindow.document.write(e+`<html><head><meta charset='${n}'><title>${r}</title></head><body></body></html>`),t.contentWindow}catch(t){}var o=document.createElement("meta");o.setAttribute("charset",n);try{var i=document.implementation.createHTMLDocument(r),u=(i.head.appendChild(o),e+i.documentElement.outerHTML);return t.setAttribute("srcdoc",u),t.contentWindow}catch(t){}return t.contentDocument.head.appendChild(o),t.contentDocument.title=r,t.contentWindow}(y,e,t,"domtoimage-sandbox");function n(t){var e;return t?((e=document.createElement("div")).innerText=t,e.innerHTML):""}}(),n=e.document,r=function(t,e){e=t.createElement(e);return t.body.appendChild(e),e.textContent="​",e}(n,t),e=function(t,e){const n={},r=t.getComputedStyle(e);return s.asArray(r).forEach(function(t){n[t]="width"===t||"height"===t?"auto":r.getPropertyValue(t)}),n}(e,r);return function(t,e){t.body.removeChild(e)}(n,r),v[t]=e}(t.tagName),l=t.style;s.asArray(i).forEach(function(t){var e,n,r,o=i.getPropertyValue(t);(o!==c[t]||u&&o!==u.getPropertyValue(t))&&(n=i.getPropertyPriority(t),e=l,o=o,n=n,r=0<=["background-clip"].indexOf(t=t),n?(e.setProperty(t,o,n),r&&e.setProperty("-webkit-"+t,o,n)):(e.setProperty(t,o),r&&e.setProperty("-webkit-"+t,o)))})}let g=null,y=null,v={}}(this);
//# sourceMappingURL=dom-to-image-more.min.js.map