<!-- JS imports -->
<!-- jQuery -->
<script type="text/javascript" src="js/jquery-2.0.2.js"></script>
<!-- jQuery UI -->
<script type="text/javascript" src="js/jquery-ui-1.10.3.custom.min.js"></script>
<script type="text/javascript" src="js/jquery/jquery.timepicker.min.js"></script>
<!-- Bootstrap + FuelUX -->
<script type="text/javascript" src="js/bootstrap/loader.min.js"></script>
<!-- DateJS -->
<script type="text/javascript" src="js/misc/date.js"></script>
<!-- MultiDatesPicker -->
<script type="text/javascript" src="js/jquery/jquery-ui.multidatespicker.js"></script>
<!-- OuterHTML -->
<script type="text/javascript" src="js/misc/outerHTML-2.1.0-min.js"></script>
<!-- Pines Notify -->
<script type="text/javascript" src="js/jquery/jquery.pnotify.min.js"></script>
<!-- jQuery tokenInput -->
<script type="text/javascript" src="js/jquery/jquery.tokeninput.js"></script>
<!-- Phone Format -->
<script type="text/javascript" src="js/bootstrap/bootstrap-formhelpers-phone.format.js"></script>
<script type="text/javascript" src="js/bootstrap/bootstrap-formhelpers-phone.js"></script>
<!-- Bootstrap Switch -->
<script type="text/javascript" src="js/bootstrap/bootstrap-switch.js"></script>
<!-- Bootbox -->
<script type="text/javascript" src="js/bootstrap/bootbox.min.js"></script>
<!-- DataTables -->
<script type="text/javascript" src="js/jquery/jquery.dataTables.min.js"></script>
<!-- Multiselect -->
<script type="text/javascript" src="js/bootstrap/bootstrap-multiselect.js"></script>
<!-- jQuery AJAX File Upload -->
<script type="text/javascript" src="js/jquery/jquery.ajaxfileupload.js"></script>
<!-- jHashTable -->
<script type="text/javascript" src="js/misc/jshashtable-3.0.js"></script>
<script type="text/javascript" src="js/misc/jshashset-3.0.js"></script>
<!-- jqPlot -->
<script type="text/javascript" src="js/jquery/jqplot/jquery.jqplot.min.js"></script>
<script type="text/javascript" src="js/jquery/jqplot/jqplot.pieRenderer.min.js"></script>
<script type="text/javascript" src="js/jquery/jqplot/jqplot.barRenderer.min.js"></script>
<script type="text/javascript" src="js/jquery/jqplot/jqplot.categoryAxisRenderer.min.js"></script>
<script type="text/javascript" src="js/jquery/jqplot/jqplot.pointLabels.min.js"></script>
<script type="text/javascript" src="js/jquery/jqplot/jqplot.canvasAxisTickRenderer.min.js"></script>
<script type="text/javascript" src="js/jquery/jqplot/jqplot.canvasTextRenderer.min.js"></script>
<script type="text/javascript" src="js/jquery/jqplot/jqplot.dateAxisRenderer.min.js"></script>
<!-- Session timeout -->
<script type="text/javascript" src="js/jquery/jquery.sessionTimeout.min.js"></script>

<!-- Footer -->
<script type="text/javascript">	
	var footerLoad = function() {
		
		setTimeout(function(){
			if ($(document).height() === $(window).height()) {
				appendFooter(58, null);
			}
		}, 0);
		
		$(window).scroll(function() {
			if ($('#footer').length > 0) return false;
			if ($(window).scrollTop() + $(window).height() >= $(document).height()) {
				appendFooter();
			}
		});
		
		$(window, document).resize(function(){
			var width = $('#footer').width();
			$('#footer').remove();
			appendFooter(null, width);
		});

		function appendFooter(initHeight, initWidth) {
			$('body')
				.append(
					$(document.createElement('div'))
						.attr('id', 'footer')
						.css({
							position: "absolute",
							top: initHeight?$(document).height() - initHeight:$(document).height(),
							display: "none"
						})
						.width(initWidth?initWidth:$(window).width())
						.append(
							$(document.createElement('div'))
								.addClass('container')
								.append(
									$(document.createElement('p'))
										.css('margin-bottom', '2px')
										.addClass('credit')
										.append(
											//Created By
											$(document.createElement('span'))
												.addClass('muted')
												.append('Created By')
												.append(
													$(document.createElement('a'))
														.attr('href', 'https://wiki.smu.edu.sg/is480/IS480_Team_wiki:_2013T1_ThunderBolt')
														.attr('target', '_blank')
														.append(
															$(document.createElement('img'))
																.attr('src', 'img/thunderbolt.png')
																.css('margin-left', '5px')
														)
												)
										)
//										.append(
//											//About Us
//											$(document.createElement('span'))
//												.addClass('footerLink')
//												.append(
//													$(document.createElement('a'))
//														.attr('href', 'about')
//														.append('About Us')
//												)
//										)
										.append(
											//Help
											$(document.createElement('span'))
												.addClass('footerLink')
												.append(
													$(document.createElement('a'))
														.attr('href', 'help')
														.append('Help')
												)
										)
								)
						)
						.show('fade', 'slow')
				);
		}
	};
	addLoadEvent(footerLoad);
</script>