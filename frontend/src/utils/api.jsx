import axios from "axios";
//import consortium from '../consortiumConfig';

//const bankCode = window._env_ ? window._env_.BANK_CODE : null;
//const baseURL = consortium[bankCode] || 'http://default-url.com';

const baseURL = "http://127.0.0.1:8080"; // Como o front deverá estar no mesmo host do banco, a comunicação será desta forma

const Api = axios.create({ //Conexão com a API
	baseURL: baseURL
});


export default Api