import { Row, Col, Form } from 'react-bootstrap';

const coluna = {
    padding: '1rem'
}

const colunaValue = {
    padding: '1rem',
    paddingLeft: '3rem',
}


export default function PaymentInput({ index, account, onInputChange }) {

    const handleChange = (e) => {
        const { name, value } = e.target;
        onInputChange(index, name, value);
    };

    return (
        <div>
            <Row>
                <Col md style={coluna}>
                    <Form.Label>Número da Conta</Form.Label>
                    <Form.Control
                        type="text"
                        name="accountCode"
                        value={account.accountCode}
                        onChange={handleChange}
                        placeholder="Número da Conta"
                    />
                </Col>
                <Col md style={colunaValue}>
                    <Form.Label>Valor</Form.Label>
                    <Form.Control
                        type="number"
                        name="value"
                        value={account.value}
                        onChange={handleChange}
                        placeholder="Valor"
                    />
                </Col>
            </Row>
        </div>


    );
}