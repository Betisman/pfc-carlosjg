 /*
 * Funciones generales 
 */
	function oculta(ID){
	  	$('#'+ID).hide();
	}
	function muestra(ID){
	  	$('#'+ID).show();
	}
	function chck(ID){
	  	$('#'+ID).attr('checked', true);  
	}
	function deschck(ID){
	  	$('#'+ID).attr('checked', false);  
	}
	function hab(ID){
	  	$('#'+ID+' :input').removeAttr('disabled');
	}
	function deshab(ID){
	  	$('#'+ID+' :input').attr('disabled', 'disabled');
	}

	function selecPaso(paso){

		if ("paso1" == paso)	{
			$("#pasoReg1").removeClass('ico1off').addClass('ico1on');
			var texto = $("#pasoReg1").html();
			$("#pasoReg1").html("<strong>"+texto+"</strong>");
		}
		if ("paso2" == paso)	{
			$("#pasoReg2").removeClass('ico2off').addClass('ico2on');
			var texto = $("#pasoReg2").html();
			$("#pasoReg2").html("<strong>"+texto+"</strong>");
		}
		if ("paso3" == paso)	{
			$("#pasoReg3").removeClass('ico3off').addClass('ico3on');
			var texto = $("#pasoReg3").html();
			$("#pasoReg3").html("<strong>"+texto+"</strong>");
		}
		if ("paso4" == paso)	{
			$("#pasoReg4").removeClass('ico4off').addClass('ico4on');
			var texto = $("#pasoReg4").html();
			$("#pasoReg4").html("<strong>"+texto+"</strong>");
		}
		if ("paso5" == paso)	{
			$("#pasoReg5").removeClass('ico5off').addClass('ico5on');
			var texto = $("#pasoReg5").html();
			$("#pasoReg5").html("<strong>"+texto+"</strong>");
		}
	}
	
	function selectDoc(tDocV){
			if ("0" == tDocV)	{
				oculta('doc1');
				oculta('doc2');
				oculta('doc3');
			}
			if ("1" == tDocV)	{
				muestra('doc1');
				oculta('doc2');
				oculta('doc3');
			}
			if ("2" == tDocV)	{
				muestra('doc2');
				oculta('doc1');
				oculta('doc3');
			}
			if ("3" == tDocV)	{
				muestra('doc3');
				oculta('doc1');
				oculta('doc2');
			}
	}
  
  function limpiaAuditoria(){  		
  		$("#iDocumento").val("");
  		$("#iGestor").val("");
  		$("#iFechaDesde").val("");
  		$("#iFechaHasta").val("");
  		$("#iServicio").val("");  		
  }
  
 /* 
 * Funciones especificas
 */
$(document).ready(function() {
		
		// JSP padronFrom de inicio
		oculta('bloqautoriz');
		chck('tipoVolanteI');
		chck('autorizadoN');
		
		// JSP registroFrom de inicio
		deshab('bloqnom');
		deshab('bloqraz');
		
		// JSP censoFrom de inicio
		selectDoc($("#tipoDoc").val()); 
		
		// JSP padronFrom
		$("#tipoVolanteI").click(function () {
			oculta('bloqautoriz')
		});
		$("#tipoVolanteF").click(function () {
			muestra('bloqautoriz');
		});
		
		// JSP registroFrom
		$("#tipoValidacionD").click(
			function () {
				hab('bloqdoc');
				deshab('bloqnom');
				deshab('bloqraz');
			});
		$("#tipoValidacionN").click(
			function () {
				deshab('bloqdoc');
				hab('bloqnom');
				deshab('bloqraz');
				
			});
		$("#tipoValidacionR").click(
			function () {
				deshab('bloqdoc');
				deshab('bloqnom');
				hab('bloqraz');
			});
			
		// JSP censoFrom
		$("#tipoDoc").change(function() {
			  selectDoc($("#tipoDoc").val()); 
		});
		
			
		if (document.getElementById("habilitar") != null)
				selecPaso(document.getElementById("habilitar").value);

});


