$(document).ready(function(){
	var ejecutar = $('.loginbox');
console.log('ejecutar: ' + ejecutar);
	if (ejecutar) {
console.log('dentro de if ejecutar: ');		
		var link;
		$('.loginbox a').each(function(){
console.log('para cada loginbox a');
console.log('- ' + ($(this).text()).toLowerCase());
			if (($(this).text()).toLowerCase().indexOf('dnie') > -1)
				link = this;
		});
console.log('tras el bucle');
		if (link) {
console.log('dentro de if link');			
			var appVersion = navigator.appVersion.toLowerCase();
console.log('appVersion: ' + appVersion);
			if (appVersion.indexOf('android') > -1) {
console.log('cambiamos el link');
				$(link).attr('href', 'intent://scan/#Intent;scheme=dnie;package=com.dnieadmin;end');				
console.log('link.href: ' + $(link).attr('href'));
			}
		}	
	}
});