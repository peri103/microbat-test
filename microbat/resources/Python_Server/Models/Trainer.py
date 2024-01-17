import torch
import torch.nn as nn
import torch.optim as optim

import random
import math
import os

from Models.supicion_predictor.DQN import DQN
from Models.supicion_predictor.ReplayMemory import ReplayMemory, Transition
from utils.Log import printMsg

class Trainer():
    def __init__(self, config):
        self.config = config
        self.batch_size = self.config["training.batch_size"]
        self.gamma = self.config["training.gamma"]
        self.eps_start = self.config["training.eps_start"]
        self.eps_end = self.config["training.eps_end"]
        self.eps_decay = self.config["training.eps_decay"]
        self.lr = self.config["training.lr"]
        self.tau = self.config["training.tau"]
        self.device = self.config["training.device"]
        self.epochs = self.config["training.epochs"]
        self.should_load_model = self.config["training.load_model"]
        self.reward_weight = self.config["training.reward_weight"]
        self.action_size = self.config["data.output_size"]
        self.epoch = 0

        self.policy_net = DQN(self.config).to(self.device).float()
        if self.should_load_model:
            model_path = self.config["training.load_path"]
            printMsg(f"Loading existing model from {model_path}", Trainer)
            self.policy_net.load_state_dict(torch.load(model_path))
            self.epoch = int(self.extract_substring(model_path, "epoch_", ".pt"))
            printMsg(f"Start training from {self.epoch}", Trainer)

        # self.target_net = DQN(self.config).to(self.device).float()
        # self.target_net.load_state_dict(self.policy_net.state_dict())
        self.optimizer = optim.AdamW(self.policy_net.parameters(), self.lr, amsgrad=True)
        self.memory = ReplayMemory(self.config)
        self.criterion = nn.SmoothL1Loss()

        self.cache = []
        self.save_interval = self.config["training.save_interval"]
        self.output_folder = self.config["training.output_path"]


    def predict(self, feature):
        sample = random.random()
        eps_threshold = self.eps_end + (self.eps_start - self.eps_end) * math.exp(-1.0 * self.epoch / self.eps_decay)
        if sample > eps_threshold:
            with torch.no_grad():
                return self.policy_net(feature).argmax().view(1,1)
        else:
            return torch.randint(0, self.action_size, size=(1,1), device=self.device, dtype=torch.long)

    def extract_substring(self, text, start_value, end_value):
        start_index = text.find(start_value)
        if start_index == -1:
            return ""  # Start value not found
        end_index = text.find(end_value, start_index + len(start_value))
        if end_index == -1:
            return ""  # End value not found
        return text[start_index + len(start_value):end_index]

    def clear_cache(self):
        self.cache = []
        
    def save_to_cache(self, node_order, input, action, reward):
        trainsition = Transition(node_order, input, action, reward)
        self.cache.append(trainsition)
    
    def update_cache_reward(self, rewardList):
        temp = []
        for node_order, reward in rewardList:
            for transition in self.cache:
                cache_node_order = transition.node_order
                if node_order == cache_node_order:
                    new_transition = Transition(node_order, transition.input, transition.action, reward)
                    temp.append(new_transition)
        self.cache = temp

    def save_cache_to_memory(self):
        for transition in self.cache:
            self.memory.push(transition)
    
    def save_model(self):
        output_file = os.path.join(self.output_folder, f"epoch_{self.epoch}.pt")
        printMsg(f"Saving model to {output_file}", Trainer)
        torch.save(self.policy_net.state_dict(), output_file)

    def isEnd(self):
        return self.epoch > self.epochs

    def optimize_model(self):
        if len(self.memory) < self.batch_size:
            printMsg(f"Not enough sample to optimize: {len(self.memory)}/{self.batch_size}", Trainer)
            return
        
        self.epoch += 1
        printMsg("Optimizing model ... ", Trainer)
        transitions = self.memory.sample(self.batch_size)
        batch = Transition(*zip(*transitions))

        input_batch = torch.stack(batch.input)
        action_batch = torch.cat(batch.action)
        reward_batch = torch.stack(batch.reward)

        printMsg("---------------------------", Trainer)
        for i in range(input_batch.shape[0]):
            input = input_batch[i]
            reward = reward_batch[i]
            action = action_batch[i]
            printMsg(f"{input[0].item()} \t {input[1].item()} \t -> action: {action.item()} \t -> {reward.item()}", Trainer)


        predicted_reward = self.policy_net(input_batch).gather(1, action_batch)
        loss = self.criterion(predicted_reward.squeeze(), reward_batch)

        self.optimizer.zero_grad()
        loss.backward()
        torch.nn.utils.clip_grad_value_(self.policy_net.parameters(), 100)
        self.optimizer.step()
        # feature_batch = torch.stack(batch.feature).to(self.device).float()
        # prob_batch = self.policy_net(feature_batch)
        # reward_batch = torch.stack(batch.reward).to(self.device).float()

        # with torch.no_grad():
        #     target_prob_batch = self.target_net(feature_batch)

        # expected_prob = (target_prob_batch * self.gamma) + reward_batch[:, None]

        # print(reward_batch.shape)
        # print(target_prob_batch.shape)
        # print(expected_prob.shape)
        # print(prob_batch.shape)
        
        # loss = self.criterion(prob_batch, expected_prob)

        # self.optimizer.zero_grad()
        # loss.backward()

        # torch.nn.utils.clip_grad_value_(self.policy_net.parameters(), 100)
        # self.optimizer.step()

        # target_net_state_dict = self.target_net.state_dict()
        # policy_net_state_dict = self.policy_net.state_dict()
        # for key in policy_net_state_dict:
        #     target_net_state_dict[key] = policy_net_state_dict[key] * self.tau + target_net_state_dict[key] * (1-self.tau)
        # self.target_net.load_state_dict(target_net_state_dict)

        if self.epoch % self.save_interval == 0:
            self.save_model()

        
