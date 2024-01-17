import sys
sys.path.append("..")

from RLModelTrainServer import RLModelTrainServer
from Models.Trainer import Trainer
from servers.FeedbackFeature import FeedbackVector
from utils.Log import printMsg, genMsg
import random
import yaml
import torch

class ForwardModelTrainServer(RLModelTrainServer):

    def __init__(self, config):
        super().__init__(config)
        self.trainer = Trainer(config)
        self.feedback_sim = self.config["training.feedback_sim"]
        
    def func(self, sock):
        printMsg("-------------------------------------------", ForwardModelTrainServer)
        mode = self.recvMsg(sock)
        if mode == RLModelTrainServer.INFERENCE_MODE:
            printMsg("Inference mode", ForwardModelTrainServer)
            printMsg("Recieve feedbacks ...", ForwardModelTrainServer)
            feedback_features = self.recieve_all_feedbacks(sock)
            feedback_vectors = [feedback_feature.node_vector for feedback_feature in feedback_features]
            while self.should_continoue(sock):
                node_order = self.recieve_node_order(sock)
                node_vector = self.recieve_node_vector(sock)
                
                probs = []
                for feedback_feature in feedback_features:
                    node_sim = self.cal_node_sim(node_vector, feedback_feature.node_vector)
                    input_feature = self.list_to_tensor([node_vector, node_sim])
                    prob = self.trainer.predict_prob(input_feature)
                    probs.append(prob)
                    self.trainer.save_to_cache(node_order, input_feature, prob, 0)
                    printMsg(f"Node: {node_order} \t forward factor: {node_sim.item()} -> {prob.item()}", ForwardModelTrainServer)
                assert len(probs) != 0, genMsg(f"prob is an empty list", ForwardModelTrainServer)
                probs = sum(probs) / len(probs)
                self.send_predictions(sock, prob.item())
                alpha = self.cal_alpha(node_vector, feedback_vectors)
                printMsg(f"Node: {node_order} \t forward alpha: {alpha.item()}", ForwardModelTrainServer)
                self.send_alpha(sock, alpha.item())
            printMsg("Finish propagation ...", ForwardModelTrainServer)
        elif mode == RLModelTrainServer.REWARD_MODE:
            printMsg("Reward mode", ForwardModelTrainServer)
            rewardList = []
            while self.should_continoue(sock):
                node_order = self.recieve_node_order(sock)
                reward = self.recieve_reward(sock)
                rewardList.append((node_order, reward))
            self.trainer.update_cache_reward(rewardList)
            self.trainer.save_cache_to_memory()
            self.trainer.clear_cache()
            self.trainer.optimize_model()
            printMsg("Finish reward ...", ForwardModelTrainServer)
        else:
            raise NotImplementedError()

    #     printMsg("-------------------------------------------", ForwardModelTrainServer)
    #     printMsg("Recieve feedbacks ...", ForwardModelTrainServer)
    #     feedback_features = self.recieve_all_feedbacks(sock)
    #     while self.should_continoue(sock):
    #         node_order = self.recieve_node_order(sock)
    #         node_vector = self.recieve_node_vector(sock)
    #         related_feedbacks = []
    #         for feedback_feature in feedback_features:
    #             feedback_node_vector = feedback_feature.node_vector
    #             if self.cosine_sim(feedback_node_vector, node_vector) > self.feedback_sim:
    #                 related_feedbacks.append(feedback_feature)

    #         if len(related_feedbacks) == 0:
    #             input_feature = self.list_to_tensor([node_vector, torch.Tensor(FeedbackVector.DIMENSION)])
    #             prob = self.trainer.predict_prob(input_feature)
    #             reward = torch.tensor([0]).to(self.device)
    #         else:
    #             probs = []
    #             rewards = []
    #             for related_feedback in related_feedbacks:
    #                 input_feature = self.list_to_tensor([node_vector, related_feedback.feedback_vector.vector])
    #                 prob = self.trainer.predict_prob(input_feature)
    #                 probs.append(prob)
    #                 rewards.append(self.cal_reward(prob, related_feedback.feedback_vector))
    #             prob = sum(probs)/len(probs)
    #             reward = sum(rewards)/len(rewards)

    #         printMsg(f"Node: {node_order} \t forward factor: {prob.item()} \t related feedback count: {len(related_feedbacks)} \t reward: {reward.item()}", ForwardModelTrainServer)
    #         self.send_prob(sock, prob.item())
    #         self.trainer.save_to_cache(input_feature, prob, reward)

    #     reward = self.recieve_reward(sock)
    #     reward = torch.tensor([reward]).to(self.device)
    #     printMsg(f"path reward: {reward.item()}", ForwardModelTrainServer)
    #     self.trainer.update_cache_reward(reward)
    #     self.trainer.save_cache_to_memory()
    #     self.trainer.clear_cache()
    #     self.trainer.optimize_model()
    #     printMsg("Finish propagation ...", ForwardModelTrainServer)
    
    # def cal_reward(self, predict, feedback_vector):
    #     if feedback_vector.is_correct_feedback():
    #         expected = 1.0
    #     else:
    #         expected = 0.0
    #     return 1 - (expected - predict) ** 2

if __name__ == "__main__":
    config_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\forward_server_config.yaml"
    with open(config_path, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = ForwardModelTrainServer(config)
    print("Server start ...")
    server.start()
    print("Server end")
