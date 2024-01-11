import {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {useAuth} from "../context/AuthContext.jsx";

const ProtectedRoute = ({children }) => {

    const { isMemberAuthenticated } = useAuth()
    const navigate = useNavigate();

    useEffect(() => {
        if(!isMemberAuthenticated()){
            navigate("/")
        }
    });
    return isMemberAuthenticated() ? children : "";
}

export default ProtectedRoute;