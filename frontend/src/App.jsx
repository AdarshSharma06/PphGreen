import { useEffect, useState } from "react";
import { api } from "./api/api";

function App() {
    const [message, setMessage] = useState("Checking backend...");

    useEffect(() => {
        api("/api/health")
            .then((response) => response.text())
            .then((data) => setMessage(data))
            .catch(() => setMessage("Backend connection failed"));
    }, []);

    return (
        <div>
            <h1>PphGreen</h1>
            <p>{message}</p>
        </div>
    );
}

export default App;