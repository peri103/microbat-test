# Changelog

## [1.3.0](https://github.com/alicializxu/microbat-test/compare/v1.2.1...v1.3.0) (2023-12-28)


### Features

* **commit:** add commit message check ci ([0098f44](https://github.com/alicializxu/microbat-test/commit/0098f446698fd2f1eef29fa41e296f4bada561fd))
* **commit:** test invalid commit message ([74f07af](https://github.com/alicializxu/microbat-test/commit/74f07af6b23c28cb3fb22201e0a98986fc928c43))
* **docs:** modify README ([cc38b79](https://github.com/alicializxu/microbat-test/commit/cc38b791067aa700b8fca5bf336c0cf78341dfb3))
* **format:** create java-code-format.yml ([5e618a0](https://github.com/alicializxu/microbat-test/commit/5e618a09a0516deaf2e1ab36b28d53a68b1d5bf5))
* **format:** google java format ([c747277](https://github.com/alicializxu/microbat-test/commit/c74727784d4212a472833654894909d54d307b49))
* **format:** google java format ([4395e6a](https://github.com/alicializxu/microbat-test/commit/4395e6a81795a2216cc58bfbbd08a184cff25d29))
* **format:** update java-code-format.yml ([7b5ec57](https://github.com/alicializxu/microbat-test/commit/7b5ec57769146ace417e5cfa34d4cef6d1fac01c))
* **release:** action called on each commit fix; fix compiling path fix; java classpath should be divided by : in linux ([737a927](https://github.com/alicializxu/microbat-test/commit/737a927231d4908929af9d37e9792d905e24352c))
* **release:** add release success flag ([83f39fa](https://github.com/alicializxu/microbat-test/commit/83f39fab41a07694ff1439a0c95ed03489c21205))
* **release:** attach jar file ([9bd461b](https://github.com/alicializxu/microbat-test/commit/9bd461b841953b167652dcbba96522269c3ca2e0))
* **release:** compiled artifact uploaded to workflow ([1a875b4](https://github.com/alicializxu/microbat-test/commit/1a875b4ffeddc45372351787a23e7770d72854fd))
* **release:** init implementation of creating release ([00a0a56](https://github.com/alicializxu/microbat-test/commit/00a0a56b4c18fa051b1069b7cfb844e8dbaa4563))
* **release:** initial implementation of github action ([614fa5d](https://github.com/alicializxu/microbat-test/commit/614fa5de757770f66322669142ddb3f50cd8a04c))
* **security:** create security-vulnerability-detection ([b791742](https://github.com/alicializxu/microbat-test/commit/b791742067f7bac1d3318fc4e1a5bb0fef4d40c6))
* **security:** rename security-vulnerability-detection to security-vulnerability-detection.yml ([4d095ed](https://github.com/alicializxu/microbat-test/commit/4d095edd5d0b05ed80810ad0febffe5054b31297))
* **unit_test:** add script to check unit test report ([9e021f7](https://github.com/alicializxu/microbat-test/commit/9e021f771583f464135f23eb992c0750c9d17084))
* **unit_test:** add test report analyzer in CI ([3699515](https://github.com/alicializxu/microbat-test/commit/3699515eac306ee399317542414f61e402be4eb1))
* **unit_test:** check whether report is also changed when java file changed ([d93c80b](https://github.com/alicializxu/microbat-test/commit/d93c80b2a20f466c99b03169d9040a21d106ad5a))
* **unit_test:** merge push and PR actions into one ([a625f2e](https://github.com/alicializxu/microbat-test/commit/a625f2e1396abcac00a0c78c17d02c1b0c92621f))
* **workflow:** add 2 workflows action files ([67c226a](https://github.com/alicializxu/microbat-test/commit/67c226ab9bf2abf35162eed19671d395f640caf5))
* **workflow:** include artifact for workflow ([18e39c3](https://github.com/alicializxu/microbat-test/commit/18e39c3bf42aa39d56d7bd80046688968ed1880e))


### Bug Fixes

* **branch:** test action delete-branch ([32571e1](https://github.com/alicializxu/microbat-test/commit/32571e12ce11313b274429493d943b20a8b06993))
* **release:** artifact for release ([15d8c35](https://github.com/alicializxu/microbat-test/commit/15d8c35a438bf61dfd80d2bb15ec1d3aa83983fc))
* **release:** attached artifact in workflow first period ([18e39c3](https://github.com/alicializxu/microbat-test/commit/18e39c3bf42aa39d56d7bd80046688968ed1880e))
* **release:** change action key ([23d7462](https://github.com/alicializxu/microbat-test/commit/23d74629e8a97e93f578958adbe2eba43a36f2fc))
* **release:** fix path of attached artifact ([3ec08ff](https://github.com/alicializxu/microbat-test/commit/3ec08ff4de4c2bf25994482be98aa80ab1e5b0f8))
* **release:** merge into one CI job to share artifacts ([1ab9d52](https://github.com/alicializxu/microbat-test/commit/1ab9d52fdbd3506a5c371d4266c7cc3953e95a93))
* **release:** modify error print command in commitlint ([a0fd1a1](https://github.com/alicializxu/microbat-test/commit/a0fd1a16a459af6248ebdb4da83d186d3522d2d0))
* **release:** print error info in annotations and test ([291de32](https://github.com/alicializxu/microbat-test/commit/291de32913f4d9f9c22fb802b1d6c6330c7a5a2a))
* **release:** set commitlint triggered only on opened pr ([4361cb3](https://github.com/alicializxu/microbat-test/commit/4361cb3ed93c6e8bcc0ea9eb7395bfaf20443a1f))
* **release:** try attach with step id ([ac477bc](https://github.com/alicializxu/microbat-test/commit/ac477bcca92b356075a57e3141b66c658b74bd3a))
* **security:** update pom.xml ([fc2bb78](https://github.com/alicializxu/microbat-test/commit/fc2bb78b56931952c5f91fa0309bc6e6511b7c01))
* **unit_test:** (maybe) correctly use checkout ([d133fb2](https://github.com/alicializxu/microbat-test/commit/d133fb24d15bfd334f423c9bcfaef8d719248808))
* **unit_test:** undo changes in .java files ([241f5d8](https://github.com/alicializxu/microbat-test/commit/241f5d816dee960cc50762ef2011e89995f3adf0))
* Use older testng jar for backward compatibility ([#252](https://github.com/alicializxu/microbat-test/issues/252)) ([59f305f](https://github.com/alicializxu/microbat-test/commit/59f305f72978b31dc04a89f5a30d63e4603ff411))
* **workflow:** add workflow file for deleting branches ([ade60a1](https://github.com/alicializxu/microbat-test/commit/ade60a1f704f70765594817173e466b4bf5bb7ef))

## [1.2.0](https://github.com/alicializxu/microbat-test/compare/v1.1.0...v1.2.0) (2023-12-28)


### Features

* **commit:** add commit message check ci ([0098f44](https://github.com/alicializxu/microbat-test/commit/0098f446698fd2f1eef29fa41e296f4bada561fd))
* **commit:** test invalid commit message ([74f07af](https://github.com/alicializxu/microbat-test/commit/74f07af6b23c28cb3fb22201e0a98986fc928c43))
* **docs:** modify README ([cc38b79](https://github.com/alicializxu/microbat-test/commit/cc38b791067aa700b8fca5bf336c0cf78341dfb3))
* **format:** create java-code-format.yml ([5e618a0](https://github.com/alicializxu/microbat-test/commit/5e618a09a0516deaf2e1ab36b28d53a68b1d5bf5))
* **format:** google java format ([c747277](https://github.com/alicializxu/microbat-test/commit/c74727784d4212a472833654894909d54d307b49))
* **format:** google java format ([4395e6a](https://github.com/alicializxu/microbat-test/commit/4395e6a81795a2216cc58bfbbd08a184cff25d29))
* **format:** update java-code-format.yml ([7b5ec57](https://github.com/alicializxu/microbat-test/commit/7b5ec57769146ace417e5cfa34d4cef6d1fac01c))
* **release:** action called on each commit fix; fix compiling path fix; java classpath should be divided by : in linux ([737a927](https://github.com/alicializxu/microbat-test/commit/737a927231d4908929af9d37e9792d905e24352c))
* **release:** add release success flag ([83f39fa](https://github.com/alicializxu/microbat-test/commit/83f39fab41a07694ff1439a0c95ed03489c21205))
* **release:** attach jar file ([9bd461b](https://github.com/alicializxu/microbat-test/commit/9bd461b841953b167652dcbba96522269c3ca2e0))
* **release:** compiled artifact uploaded to workflow ([1a875b4](https://github.com/alicializxu/microbat-test/commit/1a875b4ffeddc45372351787a23e7770d72854fd))
* **release:** init implementation of creating release ([00a0a56](https://github.com/alicializxu/microbat-test/commit/00a0a56b4c18fa051b1069b7cfb844e8dbaa4563))
* **release:** initial implementation of github action ([614fa5d](https://github.com/alicializxu/microbat-test/commit/614fa5de757770f66322669142ddb3f50cd8a04c))
* **security:** create security-vulnerability-detection ([b791742](https://github.com/alicializxu/microbat-test/commit/b791742067f7bac1d3318fc4e1a5bb0fef4d40c6))
* **security:** rename security-vulnerability-detection to security-vulnerability-detection.yml ([4d095ed](https://github.com/alicializxu/microbat-test/commit/4d095edd5d0b05ed80810ad0febffe5054b31297))
* **unit_test:** add script to check unit test report ([9e021f7](https://github.com/alicializxu/microbat-test/commit/9e021f771583f464135f23eb992c0750c9d17084))
* **unit_test:** add test report analyzer in CI ([3699515](https://github.com/alicializxu/microbat-test/commit/3699515eac306ee399317542414f61e402be4eb1))
* **unit_test:** check whether report is also changed when java file changed ([d93c80b](https://github.com/alicializxu/microbat-test/commit/d93c80b2a20f466c99b03169d9040a21d106ad5a))
* **unit_test:** merge push and PR actions into one ([a625f2e](https://github.com/alicializxu/microbat-test/commit/a625f2e1396abcac00a0c78c17d02c1b0c92621f))
* **workflow:** add 2 workflows action files ([67c226a](https://github.com/alicializxu/microbat-test/commit/67c226ab9bf2abf35162eed19671d395f640caf5))
* **workflow:** include artifact for workflow ([18e39c3](https://github.com/alicializxu/microbat-test/commit/18e39c3bf42aa39d56d7bd80046688968ed1880e))


### Bug Fixes

* **branch:** test action delete-branch ([32571e1](https://github.com/alicializxu/microbat-test/commit/32571e12ce11313b274429493d943b20a8b06993))
* **release:** artifact for release ([15d8c35](https://github.com/alicializxu/microbat-test/commit/15d8c35a438bf61dfd80d2bb15ec1d3aa83983fc))
* **release:** attached artifact in workflow first period ([18e39c3](https://github.com/alicializxu/microbat-test/commit/18e39c3bf42aa39d56d7bd80046688968ed1880e))
* **release:** change action key ([23d7462](https://github.com/alicializxu/microbat-test/commit/23d74629e8a97e93f578958adbe2eba43a36f2fc))
* **release:** fix path of attached artifact ([3ec08ff](https://github.com/alicializxu/microbat-test/commit/3ec08ff4de4c2bf25994482be98aa80ab1e5b0f8))
* **release:** merge into one CI job to share artifacts ([1ab9d52](https://github.com/alicializxu/microbat-test/commit/1ab9d52fdbd3506a5c371d4266c7cc3953e95a93))
* **release:** set commitlint triggered only on opened pr ([4361cb3](https://github.com/alicializxu/microbat-test/commit/4361cb3ed93c6e8bcc0ea9eb7395bfaf20443a1f))
* **release:** try attach with step id ([ac477bc](https://github.com/alicializxu/microbat-test/commit/ac477bcca92b356075a57e3141b66c658b74bd3a))
* **security:** update pom.xml ([fc2bb78](https://github.com/alicializxu/microbat-test/commit/fc2bb78b56931952c5f91fa0309bc6e6511b7c01))
* **unit_test:** (maybe) correctly use checkout ([d133fb2](https://github.com/alicializxu/microbat-test/commit/d133fb24d15bfd334f423c9bcfaef8d719248808))
* **unit_test:** undo changes in .java files ([241f5d8](https://github.com/alicializxu/microbat-test/commit/241f5d816dee960cc50762ef2011e89995f3adf0))
* Use older testng jar for backward compatibility ([#252](https://github.com/alicializxu/microbat-test/issues/252)) ([59f305f](https://github.com/alicializxu/microbat-test/commit/59f305f72978b31dc04a89f5a30d63e4603ff411))
* **workflow:** add workflow file for deleting branches ([ade60a1](https://github.com/alicializxu/microbat-test/commit/ade60a1f704f70765594817173e466b4bf5bb7ef))

## 1.0.0 (2023-12-28)


### Features

* **CI:** add test report analyzer in CI ([451f80a](https://github.com/alicializxu/microbat-test/commit/451f80a57f1bfdbc4f5c0924adef272e8e226345))
* **CI:** check whether report is also changed when java file changed ([a0e8d8b](https://github.com/alicializxu/microbat-test/commit/a0e8d8b7553cdf1024a648a44c1cddb6e0a8b78e))
* **CI:** merge push and PR actions into one ([43db8e1](https://github.com/alicializxu/microbat-test/commit/43db8e19c95fa3e1b29eb64508822003c2b27311))
* **test:** add script to check unittest report ([9f20229](https://github.com/alicializxu/microbat-test/commit/9f202296c5c8d3654967b79a1726694efe139bd8))
* **workflow:** action called on each commit fix; fix compiling path fix; java classpath should be divided by : in linux ([7a7691d](https://github.com/alicializxu/microbat-test/commit/7a7691d83fb82c12415d5b8f4435be56be68de7d))
* **workflow:** attach jar file ([10483dc](https://github.com/alicializxu/microbat-test/commit/10483dc5f15d7f752bb83017fd55f077cc2c36ac))
* **workflow:** compiled artifact uploaded to workflow ([a61e340](https://github.com/alicializxu/microbat-test/commit/a61e3409008516d18ceb173af6c7ebdd6c4fb95a))
* **workflow:** include artifact for workflow ([e6c8c42](https://github.com/alicializxu/microbat-test/commit/e6c8c42f0e7329436f3af0938ed1176ff1eb5e93))
* **workflow:** init implementation of creating release ([7f34bd6](https://github.com/alicializxu/microbat-test/commit/7f34bd6fc688044ab31ed0718a3f14e3e02994dd))
* **workflow:** initial implementation of github action ([a2b9380](https://github.com/alicializxu/microbat-test/commit/a2b9380edba0cc8892cf2b7f003a57f628793840))


### Bug Fixes

* **CI:** (maybe) correctly use checkout ([104fee6](https://github.com/alicializxu/microbat-test/commit/104fee6e5ca7eba10fd1f3a5d5446aeb0264772c))
* undo changes in .java files ([6e6ca89](https://github.com/alicializxu/microbat-test/commit/6e6ca8941652e432f8aac73376b769d86680dfcc))
* Use older testng jar for backward compatibility ([#252](https://github.com/alicializxu/microbat-test/issues/252)) ([59f305f](https://github.com/alicializxu/microbat-test/commit/59f305f72978b31dc04a89f5a30d63e4603ff411))
* **workflow:** add release success flag ([a05080a](https://github.com/alicializxu/microbat-test/commit/a05080a462b0939201f64a85ead28f990e7c5608))
* **workflow:** artifact for release ([5c7b2b5](https://github.com/alicializxu/microbat-test/commit/5c7b2b5b2421924df622bf250477c7e706d8117d))
* **workflow:** attached artifact in workflow first period ([e6c8c42](https://github.com/alicializxu/microbat-test/commit/e6c8c42f0e7329436f3af0938ed1176ff1eb5e93))
* **workflow:** change action key ([dc269c7](https://github.com/alicializxu/microbat-test/commit/dc269c7953856172b2cdd877948689e1e37a642d))
* **workflow:** fix path of attached artifact ([14ad9f5](https://github.com/alicializxu/microbat-test/commit/14ad9f5dde23722e44595c9f4343a1d557b6ca41))
* **workflow:** merge into one CI job to share artifacts ([db3c604](https://github.com/alicializxu/microbat-test/commit/db3c6042243590672ddc33217fab666639773f89))
* **workflow:** try attach with step id ([73a8803](https://github.com/alicializxu/microbat-test/commit/73a880307c28e06821f7e23a3cd82791ebf5d107))
