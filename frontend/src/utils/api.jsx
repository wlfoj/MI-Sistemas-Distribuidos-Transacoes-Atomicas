import axios from "axios";
import parseStringToObject from '../consortiumConfig';

function init(){
	// Obtenho
	const bankCode = window._env_ ? window._env_.BANK_CODE : null;
	var consortiumString = window._env_ ? window._env_.BANK_STRUCT : null;

	// Se não tem nada na variavel de ambiente, coloco o default
	if (consortiumString == null) {
		console.log("Entrei aqui default")
		consortiumString = '[{"bankCode": "1", "url": "http://127.0.0.1:8080/"}, {"bankCode": "2", "url": "http://127.0.0.1:8081/"}, {"bankCode": "3", "url": "http://127.0.0.1:8082/"}]';
	}
	// Faço o parse e obtenho a estrutura do tipo
	// {1: 'http://127.0.0.1:8080/', 2: 'http://127.0.0.1:8082/', 3: 'http://127.0.0.1:8081/'}
	const consortium = parseStringToObject(consortiumString) 
	//
	const baseURL = consortium[bankCode] || 'http://localhost:8080/';
	//
	console.log("Recebi " + consortiumString + typeof consortiumString)
	console.log("O meu bank_code é " + bankCode)
	console.log("O corsórcio é ")
	console.log(consortium)
	console.log("Minha  api está em " + baseURL)

	// const baseURL = "http://127.0.0.1:8080"; // Como o front deverá estar no mesmo host do banco, a comunicação será desta forma

	const Api = axios.create({ //Conexão com a API
		baseURL: baseURL
	});
	return Api;
}

const Api = init()

export default Api