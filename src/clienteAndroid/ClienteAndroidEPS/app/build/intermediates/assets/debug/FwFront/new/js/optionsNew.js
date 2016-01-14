/* Específicos de FRAMEWORK, necesarios para el funcionamiento de la validación de formularios */

var automaticScript = false; //Para validaciones
var rutaRelativa = "/FwFront";
var rutaJS = "/core/js";
var rutaImg = "/core/img";
var rutaIdiomaTemporal = rutaJS + "/idiomas/";
var arrLit = [];

var captchaReloadAction={
	carga:function(){
		var captchaReloadLink=document.getElementById("captchaReloadLink");
		var captchaImg=document.getElementById("captchaImg");
		captchaReloadLink.href = "javascript:void(0);"; 
		captchaReloadLink.onclick = function(){document.getElementById('captchaImg').src='/FrameWork/Captcha.jpg?seed='+Math.floor(Math.random()*1000000000);};
		}		
}

var validaForm={
	cargaScriptValidadacion:function(){
		if(document.getElementById("datosValidacion1"))
		{	
			if(window.valida){
				return;
			}
			else{
				automaticScript = true;
				var htmlHead = document.getElementsByTagName("head")[0];
				var eleScript = document.createElement("script");
				eleScript.src = "/FwFront/core/js/validar.js";
				htmlHead.appendChild(eleScript);
			}
			return true;
		}
		
		return false;
	}
};

var acciones={
	load:function() {	
		if (document.getElementById("datosValidacion1")){
			validaForm.cargaScriptValidadacion();
		}

		if (dom.$("captchaReloadLink")){captchaReloadAction.carga();}
		
		if ($(".tree").length != 0 && $(".col-md-4").length != 0) {
			// si estamos en el mapa web y hay bloques de tipo col-md-4 como en ciudadanos quitamos al primer enlace el icono +
			classElements = dom.getElementsByClass("tree-toggle");
			classElements[0].className = '';
		}
		
		// Si no hay resultados en colecciones que tengan coordenadas se oculta la capa "ver listado/mapa"
		if ( $('div.event-info:has(.event-location[data-latitude])').length == 0 && $('div.distritos-listado').length > 0 ) {
			classElements = dom.getElementsByClass("bg-fluid6");
			classElements[0].className = 'hidden';
		}
		
		// si la página no tiene header, main ni footer se manda a error
		if ( ($('header').length == 0) && ($('main').length == 0) && ( $('footer').length == 0) ) {
			document.write("<meta name='robots' content='noindex'>");
			window.location = "/error.htm";
		}
	}
};

//Control dom para las funciones de validacion
var dom = {
		$: function(id){
			return document.getElementById(id);
		},
		isArray: function(obj){
			return obj && !(obj.propertyIsEnumerable('length')) && typeof obj === 'object' && typeof obj.length === 'number';
		},
		getElementsById: function(strId, sep){
			var arr = [];
			sep = (sep === undefined) ? "_" : sep;
			while(dom.$(strId + sep + arr.length) !== null) {
				arr[arr.length] = dom.$(strId + sep + arr.length);
			}
			return arr;
		},
		getElementsByClass: function(searchClass, node, tag) {
			var classElements = [];			
			if (!node ){
				node = document;
			}
			if (!tag){
				tag = '*';
			}			
			var els = node.getElementsByTagName(tag);
			var elsLen = els.length;
			var pattern = new RegExp("(^|\\s)"+searchClass+"(\\s|$)");
			for (i = 0, j = 0; i < elsLen; i++) {
				if ( pattern.test(els[i].className) ) { 
					classElements[j] = els[i];
					j++;
				}
			}
			return classElements;
		},	
		onlyThisElement: function(tag, obj){
			var newObj=[];
		    for(var xx = 0; xx < obj.childNodes.length; xx++){
		    	if(obj.childNodes[xx].tagName == tag.toUpperCase()){
		    		newObj[newObj.length] = obj.childNodes[xx];
		            }
		    	}
		    return newObj;
		},
		findChilds: function(obj, tag){
			var arr = [];
			for(var x = 0; x < obj.childNodes.length; x++){
				if(obj.childNodes[x].tagName){
					if(obj.childNodes[x].tagName.toLowerCase() == tag.toLowerCase()){
						arr[arr.length] = obj.childNodes[x];
					}
				}
			}
			return arr;
		}
};

/* el validar.js a su vez necesita los ficheros de idioma que tienen los arrays de literales, se incluyen */
var idiomaAplicacion = {
		codIdioma: null,
		idiomaPorDefecto: "es",
		rutaScripts: rutaJS,							  //valor pruebas local
		rutaIdioma: rutaIdiomaTemporal,
		literal: function(l){
			return	arrLit[idiomaAplicacion.codIdioma][l]; 				
		},
		cargaIdioma: function(){
			var htmlLang=document.getElementsByTagName("html")[0].lang.toLowerCase();
			idiomaAplicacion.codIdioma = (idiomaAplicacion.codIdioma === null)? idiomaAplicacion.idiomaPorDefecto.toLowerCase():htmlLang;
			//document.write("<script  src='"+rutaRelativa+idiomaAplicacion.rutaIdioma+"lang_"+idiomaAplicacion.codIdioma+".js' language='JavaScript' ><\/script>");		
			var htmlHead = document.getElementsByTagName("head")[0];
			var eleScript = document.createElement("script");
			eleScript.src = rutaRelativa + idiomaAplicacion.rutaScripts + "/validar.js";
			eleScript.src = rutaRelativa + idiomaAplicacion.rutaIdioma + "lang_" + idiomaAplicacion.codIdioma+ ".js";
			htmlHead.appendChild(eleScript);
		} 
	};

function clear_input_html(id) {
    $('#'+id).html($('#'+id).html());
}
	
$(document).ready(function(){

	/* el nombre del usuario logado se pone ahora por JS arriba tras cargar la página */
	if(fullNameJS != null && fullNameJS != ""){
		var aImprimir = '<span class="qlh-item qlh-item-user">'+fullNameJS+'</span> <a href="/portal/site/tramites/template.LOGOUT/" class="logout"><span class="sr-only"><fwtags:fwtranslate type="LITERAL" key="estructural.cerrarSesion" defaultText="estructural.cerrarSesion" /></span></a>';
		document.getElementById("userOrFolder").innerHTML=aImprimir;
	}
	
	/* carga el idioma y las funciones de validacion */
	idiomaAplicacion.cargaIdioma();

	acciones.load();

	//A todos los input files que tengan la clase "box" les añadimos un boton de clear oculto
	//El boton se muestra cuando el input tenga algun valor metido
	//Si se quiere mostrar el boton se debe meter la funcion changeFile en el evento onchange del input
	$('input[type=file]').each(function() {
		if ($(this).attr("class")=="box") {
			var inputFileId = $(this).attr("id");
			var oculto = "";
			if ($(this).attr("value")=="" || $(this).attr("value")===undefined) {
				oculto = "hidden";
			}
			$(this).wrap('<div id="file_input_container_'+inputFileId+'"></div>');
			$(this).after('&nbsp;<a id="lnk_' + inputFileId + '" class="bold-link ' + oculto + '" href="javascript:void(0);" onclick="clearFile(\''+inputFileId+'\')">borrar</a>');
		}
	});	
	
	$('#textoanunciosEnPlazo').click(function () {
		$('#ideseaCopia2q').click();
	});
	$('#ideseaCopia2q').click(function () {
		$('#textoanunciosEnPlazo').focus();
		$('#textoanunciosFueraPlazo').val("");
	});
	$('#textoanunciosFueraPlazo').click(function () {
		$('#ideseaCopia2e').click();
	});
	$('.ui-datepicker-trigger').click(function () {
		$('#ideseaCopia2e').click();
	});
	$('#ideseaCopia2e').click(function () {
		$('#textoanunciosFueraPlazo').focus();
		$('#textoanunciosEnPlazo').val("");
	});
	$('#ideseaCopia25').click(function () {
		$('#textoanunciosFueraPlazo').val("");
		$('#textoanunciosEnPlazo').val("");
	});
})

//Borrar el valor de un input file y oculta el boton de borrar asociado
function clearFile(inputFileId) {
	clear_input_html('file_input_container_'+inputFileId);
	$("#img_" + inputFileId).addClass("hidden");
	$("#lnk_" + inputFileId).addClass("hidden");
}

//Esta funcion muestra en boton de Borrar en un input file cuando el input tiene valor
//Se debe meter en el evento onChange de los input file en los que se desee que se muestre el boton 
function changeFile(inputFileId) {
	var input = document.getElementById(inputFileId);
	if (input.value != '') {
		$("#img_" + inputFileId).removeClass("hidden");
		$("#lnk_" + inputFileId).removeClass("hidden");
	}
}


