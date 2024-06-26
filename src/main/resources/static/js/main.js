const SERVER_URL = 'http://localhost:8080/api/v1/';

const data = {
  setting: "Faerûn",
  character: "A drunk dwarf",
  history: "",
  action: "",
}

document.getElementById('form-setting').addEventListener('submit', setSetting);
document.getElementById('form-character').addEventListener('submit', setCharacter);
document.getElementById('form-story').addEventListener('submit', getStory);
document.getElementById('btn-reset').addEventListener('click', resetStory);
document.getElementById('btn-lightmode').addEventListener('click', toggleLightMode);

function setSetting(event) {
  event.preventDefault();
  data.setting = event.target.setting.value ? event.target.setting.value : "Faerûn";
  document.getElementById('card-setting').style.display = 'none';
  document.getElementById('card-character').style.display = 'block';
}


function setCharacter(event) {
  event.preventDefault();
  data.character = event.target.character.value? event.target.character.value : "A drunk dwarf";
  document.getElementById('card-character').style.display = 'none';
  document.getElementById('card-story').style.display = 'block';

  initPrompt();
}


async function initPrompt() {
  const URL = `${SERVER_URL}story`
  const spinner = document.getElementById('spinner1');

  try {
    spinner.style.display = "block";
    const res = await fetch(URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    }).then(handleHttpErrors)
    document.getElementById('chat-history').insertAdjacentHTML('beforeend', `<li class="list-group-item chat-response"></li>`);
    typeWriter(res.answer, document.getElementById('chat-history').lastChild);
    data.history = res.answer;

  } catch (error) {
    console.log(error);
  } finally {
    spinner.style.display = "none";
  }
}

async function getStory(event) {
  event.preventDefault();
  const URL = `${SERVER_URL}story`
  const spinner = document.getElementById('spinner1');
  const chat = document.getElementById('chat-history');

  data.action = event.target.action.value;

  try {
    spinner.style.display = "block";
    const res = await fetch(URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    }).then(handleHttpErrors)

    chat.insertAdjacentHTML('beforeend', `<li class="list-group-item chat-action"></li>`);
    typeWriter(event.target.action.value, chat.lastChild);

    chat.insertAdjacentHTML('beforeend', `<li class="list-group-item chat-response"></li>`);
    typeWriter(res.answer, chat.lastChild);
    data.history = data.history + res.answer;
    event.target.action.value = "";
  } catch (error) {
    console.log(error);
  } finally {
    spinner.style.display = "none";
  }
}

async function handleHttpErrors(res) {
  if (!res.ok) {
    const errorResponse = await res.json();
    const msg = errorResponse.message ? errorResponse.message : "No error details provided"
    throw new Error(msg)
  }
  return res.json()
}

function resetStory(){
  document.getElementById('card-setting').style.display = 'block';
  document.getElementById('card-character').style.display = 'none';
  document.getElementById('card-story').style.display = 'none';
  document.getElementById('chat-history').innerHTML = "";
  data.setting = "Faerûn";
  data.character = "A drunk dwarf";
  data.history = "";
  data.action = "";
}

function typeWriter(text, element) {
  let i = 0;
  function type() {
    element.scrollIntoView({ behavior: 'smooth', block: 'end' });
    if (i < text.length) {
      element.innerHTML += text.charAt(i);
      i++;
      setTimeout(type, 25);
    }
  }
  type();
}

function toggleLightMode(){
  document.querySelectorAll("*").forEach((e)=>e.classList.toggle('light-mode'));
  const btn = document.getElementById('btn-lightmode');
  btn.innerHTML = btn.innerHTML === "Set Dark Mode" ? "Set Light Mode" : "Set Dark Mode";
  
}