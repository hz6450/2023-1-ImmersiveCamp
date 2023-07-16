from flask import Flask, request, render_template

app = Flask(__name__)

import torch
import cv2
import matplotlib.pyplot as plt
from src.Models import Unet
import os    
os.environ['KMP_DUPLICATE_LIB_OK']='True'    

n_classes = 2
device = 'cuda' if torch.cuda.is_available() else 'cpu'

# damage models
labels = ['Breakage_3', 'Crushed_2', 'Scratch_0', 'Seperated_1']
models = []

for label in labels:
    model_path = f'models/[DAMAGE][{label}]Unet.pt'

    model = Unet(encoder='resnet34', pre_weight='imagenet', num_classes=n_classes).to(device)
    model.model.load_state_dict(torch.load(model_path, map_location=torch.device(device)))
    model.eval()

    models.append(model)

@app.route('/', methods=['GET'])
def index():
    return render_template('index.html')

@app.route('/', methods=['POST'])
def upload_file():
    file = request.files['image']
    image_path = 'static/' + file.filename
    file.save(image_path)

    img  = cv2.imread(image_path)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = cv2.resize(img, (256, 256))

    plt.figure(figsize=(8, 8))
    plt.imshow(img)
    plt.savefig('static/img_input.png')

    img_input = img / 255.
    img_input = img_input.transpose([2, 0, 1])
    img_input = torch.tensor(img_input).float().to(device)
    img_input = img_input.unsqueeze(0)

    fig, ax = plt.subplots(1, 5, figsize=(24, 10))

    ax[0].imshow(img)
    ax[0].axis('off')

    outputs = []

    for i, model in enumerate(models):
        output = model(img_input)

        img_output = torch.argmax(output, dim=1).detach().cpu().numpy()
        img_output = img_output.transpose([1, 2, 0])

        outputs.append(img_output)

        ax[i+1].set_title(labels[i])
        ax[i+1].imshow(img_output, cmap='jet')
        ax[i+1].axis('off')

    fig.set_tight_layout(True)
    plt.savefig('static/img_output.png')

    total_area = 0
    price_table = [
        100, # Breakage_3
        200, # Crushed_2
        50,  # Scratch_0
        120, # Seperated_1
    ]

    for i, price in enumerate(price_table):
        area = outputs[i].sum()
        total_area += area

        print(f'{labels[i]}:\t영역: {area}\t가격:{area * price}원')

    total_price = total_area * 350
    result_msg = f'이해준 고객님, 총 수리비는 {total_price}원 입니다!'

    return render_template('index.html', img_input=image_path, img_output='static/img_output.png', result_msg=result_msg)

if __name__ == '__main__':
    app.run(debug=True)
