import axios from "axios";

const mode = (import.meta.env.VITE_MODE);
const http = axios.create({
    baseURL: mode === 'backend' ? '' : '/api'
});

http.interceptors.request.use(
    (config) => {
        return config;
    },
    (error) => {
        return Promise.reject(error)
    }
)
http.interceptors.response.use(
    (response) => {
        // 如果后端有自定义响应体, 则返回内层的业务数据
        return response;
    },
    (error) => {
        return Promise.reject(error.response.data.message)
    }
)

export default http;