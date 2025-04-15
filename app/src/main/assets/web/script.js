const serverUrl = window.location.origin;


var mFiles = [];

function uploadMyFile(file, progressBar, hint, callback) {
    const formData = new FormData();
    formData.append("file", file);

    const fileType = file.type;
    const headers = new Headers();
    headers.append("type", fileType);

    progressBar.style.display = "block";

    fetch(`${serverUrl}/upload`, {
        method: "POST",
        body: formData,
        headers: headers
    })
    .then(res => res.text())
    .then(responseText => {
        console.log(responseText);
        callback();
    })
    .catch(error => {
        console.error("Upload failed:", error);
        alert("Upload failed.");
        callback();
    });
}

function startUploadingFiles() {
    const fileInput = document.getElementById("fileInput");
    const progressBar = document.getElementById("uploadProgress");
    const hint = document.getElementById("uploadHint");

  if (fileInput.files.length > 0) {
           mFiles = [...mFiles, ...Array.from(fileInput.files)];
             fileInput.value = "";
      } else if (mFiles && mFiles.length > 0) {
          file = mFiles[0];
       }

    if (mFiles.length === 0) {
        alert("No files to upload");
        return;
    }

    hint.style.display = 'block';

    let fileIndex = 0;

    function uploadNextFile() {
        if (fileIndex < mFiles.length) {
            uploadMyFile(mFiles[fileIndex], progressBar, hint, () => {
                fileIndex++;
                uploadNextFile();
            });
        } else {

            alert("All files uploaded.")
            mFiles = [];

            console.log("All files uploaded.");
            progressBar.style.display = "none";
            hint.style.display = 'none';
        }
    }

    uploadNextFile();
}


function loadFiles() {
    fetch(`${serverUrl}/files`)
        .then(res => res.json())
        .then(files => {
            let listHTML = files
                .map(f => {
                    let fileName = f.split('/').pop();
                    return `<a href="${serverUrl}/download/${encodeURIComponent(f)}">${fileName}</a><br>`;
                })
                .join("");
            document.getElementById("fileList").innerHTML = listHTML;
        })
        .catch(console.error)
        .finally(() => {
                    spinner.style.display = "none";
                });
}


function submitPin() {
  const pin = document.getElementById("pinInput").value;
  fetch("/login", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `pin=${pin}`
  })
  .then(res => {
    if (res.status === 200) {
      window.location.href = "/index.html";// Redirect to homepage
    } else {
      return res.text().then(text => {
        document.getElementById("errorMsg").innerText = text || "Invalid PIN";
      });
    }
  })
  .catch(() => {
    document.getElementById("errorMsg").innerText = "Error connecting to server";
  });
}

    const dropArea = document.getElementById('drop-area');
    const fileInput = document.getElementById('file-input');

    dropArea.addEventListener('dragenter', highlight, false);
    dropArea.addEventListener('dragover', highlight, false);
    dropArea.addEventListener('dragleave', unhighlight, false);
    dropArea.addEventListener('drop', handleDrop, false);

    function highlight(e) {
            dropArea.classList.add('highlight');
            e.preventDefault();
            e.stopPropagation();
        }

      function unhighlight(e) {
             dropArea.classList.remove('highlight');
             e.preventDefault();
             e.stopPropagation();
        }

     function handleDrop(e) {
        const hint = document.getElementById("uploadHint");

        unhighlight(e);
        let dt = e.dataTransfer;
        let files = dt.files;
        hint.style.display = 'block';
        hint.textContent = `Ready to upload files: ${files.length}`
        mFiles = files

    }

window.onload = loadFiles;
