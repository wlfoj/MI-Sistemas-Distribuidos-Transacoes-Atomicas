import { useState, useEffect } from 'react';
import { Container, Row, Col, Alert, Button, Form } from 'react-bootstrap';
import Api from '../utils/api';
import AccountInput from './AccountInput';
import { Link } from 'react-router-dom';


const container = {
    backgroundColor: '#FFFFFF', padding: '2rem', marginTop: '1.5rem', marginBottom: '1.5rem',
    boxShadow: '4px 4px 4px lightgray', borderRadius: '6px', width: '60%'
}

const botao = {
    backgroundColor: '#5E3797', border: '0', width: '70%'
}

export default function Transfer() {

    const [cpf] = useState(localStorage.getItem('cpf'));
    const [bankId] = useState(localStorage.getItem('bankCode'));

    const [accounts, setAccounts] = useState([]);
    const [destinyAccount, setDestinyAccount] = useState({
        bankCode: '',
        accountCode: '',
        value: 0
    });

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");


    useEffect(() => { // Função para pegar a tela de transação

        Api.get(`/user/accounts/${cpf}`)

            .then((body) => {
                console.log(body.data)
                setAccounts(body.data.accountsToUse);
                setError("");
            }).catch((err) => {
                setMessage("");
                setError("Erro: " + err);
                console.log("Erro: " + err);
            });
    }, [cpf])

    const handleDestinyInputChange = (field, value) => {
        setDestinyAccount({
            ...destinyAccount,
            [field]: value
        });
    };

    const handleInputChange = (index, field, value) => {
        const newAccounts = [...accounts];
        newAccounts[index][field] = value;
        setAccounts(newAccounts);
    };

    const handleSubmit = (e) => {
        e.preventDefault(); // Para dar reload após o submit só retirar isso

        const accountResponse = {
            transactionType: "TRANSFER",
            source: {
                "bankCode": bankId,
                "cpf": cpf
            },
            operations: [
                {
                    operationType: "DEPOSIT",
                    bankCode: destinyAccount.bankCode,
                    accountCode: destinyAccount.accountCode,
                    value: parseFloat(destinyAccount.value)
                },
                ...accounts.map(account => ({
                    operationType: "WITHDRAW",
                    bankCode: account.bankCode,
                    accountCode: account.accountCode,
                    value: parseFloat(account.value)
                }))
            ]
        };

        console.log(accountResponse)

        Api.post("/user/openTransaction", accountResponse)

            .then((body) => {
                console.log(body.data)
                setError("");
                setMessage(body.data.message);
                setTimeout(() => {
                    setMessage("");
                }, 15000)

            }).catch((err) => {
                setMessage("");
                setError("Erro: " + err);
                console.log("Erro: " + err);
            });
    };

    return (
        <Form onSubmit={handleSubmit}>
            <Container style={container}>
                <Row>
                    <Col style={{ textAlign: 'right' }}>
                        <Link to="/home">
                            <Button style={{ backgroundColor: '#5E3797', border: '0' }}>
                                Home
                            </Button>
                        </Link>
                    </Col>
                </Row>

                <Row style={{ padding: '1rem' }}>
                    <h1 className="display-6" style={{ textAlign: 'center' }}> Transferência </h1>
                    {error ? (
                        <Alert size="sm" variant="danger">{error}</Alert>
                    ) : null}
                    {message ? (
                        <Alert size="sm" variant="success"> {message} </Alert>
                    ) : null}
                </Row>

                <h1 className="display-6" style={{ fontSize: '1.4rem' }}> Banco de Origem </h1>
                <Row>
                    {accounts.map((account, index) => (
                        <AccountInput
                            key={index}
                            index={index}
                            account={account}
                            onInputChange={handleInputChange}
                        />

                    ))}
                </Row>

                <h1 className="display-6" style={{ fontSize: '1.4rem', marginTop: '2rem' }}> Banco de Destino </h1>

                <Row>
                    <AccountInput
                        key="source"
                        index="source"
                        account={destinyAccount}
                        onInputChange={(index, field, value) => handleDestinyInputChange(field, value)}
                    />

                </Row>

                <Col style={{ textAlign: 'center', marginTop: '2rem' }}>
                    <Button size="lg" style={botao} type="submit"> Confirmar </Button>
                </Col>
            </Container>
        </Form>
    );
}