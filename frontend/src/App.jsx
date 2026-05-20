import { useEffect, useState } from 'react'
import logo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
  const [count, setCount] = useState(0)
  const [todos, setTodos] = useState([]);
  const [taskdescription, setTaskdescription] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [duedate, setDuedate] = useState("");
  const [priority, setPriority] = useState("MITTEL");

  const handleSubmit = event => {
    event.preventDefault();
    console.log("Sending task description to Spring-Server: "+taskdescription);
    fetch("http://localhost:8080/tasks", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ taskdescription: taskdescription, priority: priority, duedate: duedate })
    })
    .then(response => {
      console.log("Receiving answer after sending to Spring-Server: ");
      console.log(response);
      window.location.href = "/";
      setTaskdescription("");
      setDuedate("");
      setPriority("MITTEL");
    })
    .catch(error => console.log(error))
  }

  const handleChange = event => {
    setTaskdescription(event.target.value);
  }

  useEffect(() => {
    const url = searchQuery
      ? `http://localhost:8080/tasks/search?q=${encodeURIComponent(searchQuery)}`
      : "http://localhost:8080/";
    fetch(url).then(response => response.json()).then(data => {
      setTodos(data);
    });
  }, [searchQuery]);

  const handleDelete = (event, taskdescription) => {
    console.log("Sending task description to delete on Spring-Server: "+taskdescription);
    fetch(`http://localhost:8080/delete`, {
      method: "POST",
      body: JSON.stringify({ taskdescription: taskdescription }),
      headers: {
        "Content-Type": "application/json"
      }
    })
    .then(response => {
      console.log("Receiving answer after deleting on Spring-Server: ");
      console.log(response);
      window.location.href = "/";
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
            onChange={handleChange}
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