function validarCheck(){
				var f = document.fotmApMail1;
				var listaCheck = [];
				var listaVacia = true;
				var contCheck = 0;
					if (document.getElementById("0").checked) {		
						insertar("true");	
						contCheck++;
						listaVacia = false;
					} else {
						insertar("false");	
					}
				if (listaVacia) {
					eliminarListaCheck();
					alert ("Debe seleccionar al menos una inscripcion.");
				} else if (contCheck > 25) { 
					eliminarListaCheck();
					alert ("El n?mero m?ximo de recibos que pueden ser adheridos al PAC es 25. Usted acaba de seleccionar un total de "+contCheck+". Por favor, limite su selecci?n al m?ximo permitido.");
				} else {
					f.submit();
				}
}
function validarRadio() {
	var selec = false;
	var periodo_pago;
	var f = document.fotmApMail1;
	
	for (i=0;i<f.periodo_pagos.length;i++){
       	if (f.periodo_pagos[i].checked) {
          	selec = true;
          	
          	periodo_pago = f.periodo_pagos[i].value;	
	    }
 	}
 	
 	if (selec) {
 		return true;
 	} else {
 		alert("Debe seleccionar una periodicidad.");
 		return false;
 	}
}
			function comprobarImporteOk() {
			
				var f = document.fotmApMail1;					
				var euro = String.fromCharCode(8364);		
				var imp_prop_contri_param = trim(f.imp_prop_contri_param.value);
				
				if (imp_prop_contri_param && imp_prop_contri_param.length != 0) { //Si no est? vac?o		
					
					imp_prop_contri_param = imp_prop_contri_param.replace(",",".");
					
					if(isNaN(imp_prop_contri_param)) {
						alert("El dato introducido no es un importe v?lido.");
						document.fotmApMail1.imp_prop_contri_param.focus();
						document.fotmApMail1.imp_prop_contri_param.select();
						return false;
					}
					
					imp_prop_contri_param = formatearImporte(imp_prop_contri_param);
					
					//Hay que volver a colocarle los puntos para que funcione el parseFloat
					var baseCalculo = parseFloat(f.baseCalculo.value.replace(",","."));
					var ipcp = parseFloat(imp_prop_contri_param.replace(",","."));
					
					if (ipcp < 6 || ipcp > baseCalculo) {
						alert("Recuerde que el importe propuesto no debe ser inferior a 6 "+ euro +" ni superior a la suma total de los importes: " + f.baseCalculo.value + " " + euro + ".");
						f.imp_prop_contri_param.focus();
						f.imp_prop_contri_param.select();
						return false;
					}
				} 
				
				f.imp_prop_contri_param.value = imp_prop_contri_param;
				return true;
			}
		function setRadioValue(objeto, keyValue){
		if(typeof(objeto.length)=='undefined' || objeto.length <= 1){
				objeto.checked = true;
		} else {
		for(var i=0; i<objeto.length; i++){
			if(objeto[i].value == keyValue){
				objeto[i].checked = true;
		return true;
		}
		}
}
}
			
function formatearImporte(valor) {
	
	valor = valor.toString().replace(/$|\,/g,'');
	
	sign = (valor == (valor = Math.abs(valor)));
	valor = Math.floor(valor*100+0.50000000001);
	cents = valor%100;
	valor = Math.floor(valor/100).toString();
	if(cents<10)
	cents = "0" + cents;
	/*for (var i = 0; i < Math.floor((valor.length-(1+i))/3); i++)
	valor = valor.substring(0,valor.length-(4*i+3))+','+
	valor.substring(valor.length-(4*i+3));*/
	return (((sign)?'':'-') + valor + ',' + cents);
}