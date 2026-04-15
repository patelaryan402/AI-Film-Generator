var LABELS = {
  chat: "Story Development",
  script: "Generated Script",
  scenes: "Scene Breakdown",
  narration: "Narration Script"
};

// ===== TYPEWRITER FUNCTION =====
function typeWriter(element, text, speed = 15) {
  element.textContent = "";
  let i = 0;

  function typing() {
    if (i < text.length) {
      element.textContent += text.charAt(i);
      element.scrollTop = element.scrollHeight; // auto scroll
      i++;
      setTimeout(typing, speed);
    } else {
      element.classList.remove("typing");
    }
  }

  element.classList.add("typing");
  typing();
}

function generate(type) {
  var idea = document.getElementById("idea").value;

  if (!idea || idea.trim() === "") {
    alert("Please enter a film idea first!");
    return;
  }

  // ===== SHOW LOADING =====
  document.getElementById("loading").style.display = "block";
  document.getElementById("result-box").classList.remove("visible");
  document.getElementById("result-text").textContent = "";

  // Call Java server
  var xhr = new XMLHttpRequest();
  xhr.open("POST", "http://localhost:8080/" + type, true);
  xhr.setRequestHeader("Content-Type", "text/plain");

  xhr.onreadystatechange = function () {
    if (xhr.readyState === 4) {

      // ===== HIDE LOADING =====
      document.getElementById("loading").style.display = "none";

      if (xhr.status === 200) {
        document.getElementById("result-label").textContent =
          LABELS[type] || "Result";

        let response = xhr.responseText;

        // Fix <center> tags
        response = response.replace(/<center>(.*?)<\/center>/g,
          '<div style="text-align:center; font-weight:bold;">$1</div>');

        // ===== TYPEWRITER OUTPUT =====
        typeWriter(document.getElementById("result-text"), response);

      } else {
        document.getElementById("result-label").textContent = "Error";
        document.getElementById("result-text").textContent =
          "Could not connect to Java server.\n\n" +
          "Make sure you started the server!\n" +
          "Run: java -cp out;lib\\gson-2.10.1.jar;lib\\okhttp-4.12.0.jar;lib\\okio-3.6.0.jar Main\n\n" +
          "Status: " + xhr.status;
      }

      // ===== SHOW RESULT BOX =====
      document.getElementById("result-box").classList.add("visible");
    }
  };

  xhr.onerror = function () {
    document.getElementById("loading").style.display = "none";
    document.getElementById("result-label").textContent = "Connection Error";
    document.getElementById("result-text").textContent =
      "Cannot reach Java server at localhost:8080.\n" +
      "Make sure the server is running first!";
    document.getElementById("result-box").classList.add("visible");
  };

  xhr.send(idea.trim());
}

// ===== COPY FUNCTION =====
function copyResult() {
  var text = document.getElementById("result-text").textContent;
  var temp = document.createElement("textarea");
  temp.value = text;
  document.body.appendChild(temp);
  temp.select();
  document.execCommand("copy");
  document.body.removeChild(temp);

  var btn = document.querySelector(".copy-btn");
  btn.textContent = "Copied!";
  setTimeout(function () {
    btn.textContent = "Copy";
  }, 2000);
}