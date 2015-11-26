var serviceUrl = "servlet";
var pendingSuggestAction = null;

function suggest() {
  var text = $("#inputarea").attr("value");
  $.ajax({
    type: "POST",
    url: serviceUrl,
    data: {text: text, withdebuginfo: "false"},
    dataType: "json",
    success: function(data, status) {
      var resultHtml = "";
      var maxWeight = 0;
      var maxFontSize = 28;
      for (var i = 0; i < data.results.length; i++) {
        var tag = data.results[i].text;
        var weight = data.results[i].weight;
        var css = "";
        if (weight > maxWeight) {
          maxWeight = weight;
        }
        // Use the weight to decide the size of the tag.
        var fontSize = maxFontSize * weight / maxWeight;
        if (fontSize < 12)
          fontSize = 12;
        css = "font-size:" + fontSize + "px;line-height:34px;";
        if (weight < 0.2 * maxWeight) {
          css += "background-color:#DDDDDD;color:#555555";
        } else {
          css += "background-color:#FFCC33;";
        }
        var tagHtml = "<div id='tag" + i + "' style=\"" + css + "\">" + data.results[i].text + "</div>";
        // resultHtml = (i % 2 == 0) ? resultHtml + tagHtml : tagHtml + resultHtml;
        resultHtml = resultHtml + tagHtml;
      }
      if (data.results.length == 0) {
        resultHtml = "<div id='tag0' style='color:grey;line-height:34px'>无标签</div>";
      }
      $("#suggested").html(resultHtml + "<div style='float:none;clear:both'></div>");
      fadeInNext(0, Math.max(1, data.results.length));
      $("#debuginfo").html(data.debugInfo);
    }
   });
}

function fadeInNext(i, max) {
  if (i == max)
    return;
  var tagId = '#tag' + i;
  $(tagId).fadeIn(40 + i * 5, function() {
    fadeInNext(i+1, max);
  });
}

function prepareUpdate() {
  if (pendingSuggestAction != null)
    window.clearTimeout(pendingSuggestAction);
  pendingSuggestAction = window.setTimeout(suggest, 500);
}

function showDebug() {
  $("#showdebug").css("display", "none");
  $("#hidedebug").css("display", "inline");
  $("#debuginfo").fadeIn(100);
}

function hideDebug() {
  $("#showdebug").css("display", "inline");
  $("#hidedebug").css("display", "none");
  $("#debuginfo").fadeOut(100);
}
// REGISTER HANDLERS.

$(document).ready(function() {
  $("#inputarea").keyup(prepareUpdate);
  $("#showdebug").click(showDebug);
  $("#hidedebug").click(hideDebug);
})
