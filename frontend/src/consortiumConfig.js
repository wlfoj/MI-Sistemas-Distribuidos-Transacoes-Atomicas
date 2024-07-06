function parseStringToObject(str) {
  try {
    // Analisa a string JSON para um array de objetos
    const array = JSON.parse(str);
    
    // Inicializa um objeto vazio
    const obj = {};

    // Para cada objeto no array, adiciona ao objeto final usando bankCode como chave
    array.forEach(item => {
      obj[item.bankCode] = item.url;
    });

    return obj;
  } catch (error) {
    console.error('Erro ao analisar a string JSON:', error);
    return null;
  }
}

export default parseStringToObject;