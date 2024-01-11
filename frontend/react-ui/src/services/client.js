import axios from 'axios';

// get jwtToken to perform protected methods
const getAuthConfig = () => ({
    headers: {
        authorization: `Bearer ${localStorage.getItem("access_token")}`
    }
})

export const getMembers = async () => {
    try{
        return await axios.get(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/members`,
            getAuthConfig())
    } catch(err){
        throw err;
    }
}

export const saveMember =  async (member) => {
    try {
        return await axios.post(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/members`,
            member
        )
    } catch (e) {
        throw e;
    }
}

export const updateMember = async (id, update) => {
    try {
        return await axios.put(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/members/${id}`,
            update,
            getAuthConfig()
        )
    } catch (e) {
        throw e;
    }
}

export const deleteMember = async (id) => {
    try {
        return await axios.delete(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/members/${id}`,
            getAuthConfig()
        )
    } catch (e) {
        throw e;
    }
}

export const login = async (usernameAndPassword) => {
    try {
        return await axios.post(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/login`,
            usernameAndPassword
        );
    } catch (e) {
        throw e;
    }
}