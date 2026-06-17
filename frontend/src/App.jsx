import { useEffect, useState } from 'react'
import logo from './assets/react.svg'
import './App.css'

const API_BASE = "http://localhost:8080/api/v1";

function App() {
  const [todos, setTodos] = useState([]);
  const [taskdescription, setTaskdescription] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [duedate, setDuedate] = useState("");
  const [priority, setPriority] = useState("MITTEL");

  const handleSubmit = event => {
    event.preventDefault();
    console.log("Aufgabe wird an Spring-Server gesendet: " + taskdescription);
    fetch(`${API_BASE}/tasks`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ taskdescription: taskdescription, priority: priority, duedate: duedate })
    })
    .then(response => {
      console.log("Antwort vom Spring-Server erhalten: ");
      console.log(response);
      setTaskdescription("");
      setDuedate("");
      setPriority("MITTEL");
      fetchTasks();
    })
    .catch(error => console.log(error))
  }

  const fetchTasks = () => {
    const url = searchQuery
      ? `${API_BASE}/tasks/search?q=${encodeURIComponent(searchQuery)}`
      : `${API_BASE}/tasks`;
    fetch(url)
      .then(response => response.json())
      .then(data => setTodos(data));
  }

  useEffect(() => {
    fetchTasks();
  }, [searchQuery]);

  const handleDelete = (event, taskdescription) => {
    console.log("Aufgabe wird auf Spring-Server gelöscht: " + taskdescription);
    fetch(`${API_BASE}/tasks/delete`, {
      method: "POST",
      body: JSON.stringify({ taskdescription: taskdescription }),
      headers: {
        "Content-Type": "application/json"
      }
    })
    .then(response => {
      console.log("Antwort nach dem Löschen erhalten: ");
      console.log(response);
      fetchTasks();
    })
    .catch(error => console.log(error))
  }

  const renderTasks = (todos) => {
    return (
      <ul className="todo-list">
        {todos.map((todo, index) => (
          <li key={todo.taskdescription}>
            <span className={`priority-label priority-${(todo.priority || 'MITTEL').toLowerCase()}`}>
              {todo.priority || 'MITTEL'}
            </span>
            <span>{"Task " + (index+1) + ": "+ todo.taskdescription}</span>
            {todo.duedate && <span className="duedate-label">Fällig: {todo.duedate}</span>}
            <button onClick={(event) => handleDelete(event, todo.taskdescription)}>&#10004;</button>
          </li>
        ))}
      </ul>
    );
  }

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <h1>ToDo Liste</h1>
        <form onSubmit={handleSubmit} className='todo-form'>
          <label htmlFor="taskdescription">Neues Todo anlegen:</label>
          <input
            type="text"
            value={taskdescription}
            onChange={(e) => setTaskdescription(e.target.value)}
          />
          <select value={priority} onChange={(e) => setPriority(e.target.value)} className="priority-select">
            <option value="HOCH">Hoch</option>
            <option value="MITTEL">Mittel</option>
            <option value="TIEF">Tief</option>
          </select>
          <input
            type="date"
            value={duedate}
            onChange={(e) => setDuedate(e.target.value)}
            className="duedate-input"
          />
          <button type="submit">Absenden</button>
        </form>
        <div className="search-container">
          <input
            type="text"
            className="search-input"
            placeholder="Tasks durchsuchen..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <div>
          {renderTasks(todos)}
        </div>
      </header>
    </div>
  );
}

export default App